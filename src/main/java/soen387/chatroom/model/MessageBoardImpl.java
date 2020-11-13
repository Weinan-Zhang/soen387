package soen387.chatroom.model;

import javafx.util.Pair;
import soen387.chatroom.persistence.AttachmentDAO;
import soen387.chatroom.persistence.PostDAO;
import soen387.chatroom.persistence.TagDAO;

import javax.sql.rowset.serial.SerialBlob;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.Blob;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MessageBoardImpl implements MessageBoard {
    public static final String USER_LOCAL_FILE_PATH = "D:\\study\\concordia\\2020\\fall\\SOEN387\\project\\chatroom\\src\\main\\java\\soen387\\chatroom\\localization\\user.json";
    private static PostDAO postDAO;
    private static AttachmentDAO attachmentDAO;
    private static TagDAO tagDAO;

    public MessageBoardImpl(PostDAO postDAO, AttachmentDAO attachmentDAO, TagDAO tagDAO){
        this.postDAO = postDAO;
        this.attachmentDAO = attachmentDAO;
        this.tagDAO = tagDAO;
    };

    @Override
    public List<User> userAuthenticate(String username, String password) throws FileNotFoundException, NoSuchAlgorithmException {
        String passwordMd5 = User.md5gen(password);
        List<User> userdata = User.deserializationFromJson(USER_LOCAL_FILE_PATH);
        List<User> userMatch = userdata.stream().filter(user -> user.username.equals(username) && user.password.equals(passwordMd5)).collect(Collectors.toList());
        return userMatch;
    }

    @Override
    public void userSignup() throws NoSuchAlgorithmException, IOException {
        User user1 = new User("admin", "admin@admin.com", "123456",true);
        User user2 = new User("008z", "008z@sina.com", "45678",true);
        User user3 = new User("40043261", "40043261@40043261.com", "40043261",true);
        List<User> users = new ArrayList<User>();
        users.add(user1);
        users.add(user2);
        users.add(user3);
        User.serializationToJson(users, USER_LOCAL_FILE_PATH);
    }

    @Override
    public List<Pair<Post, List<Pair<Integer, String>>>> listPosts(int startIdx, int postsPerPage) throws SQLException, ClassNotFoundException {
        return this.postDAO.findPosts(startIdx, postsPerPage);
    }

    @Override
    public int createPost(Post post) throws SQLException, ClassNotFoundException {
        int pid = this.postDAO.addNewPost(post.getUserId(), post.getUsername(),post.getTitle(), post.getContent());
        if(post.getHashTags().size()>0){
            post.getHashTags().stream().forEach(tag->{
                try {
                    int tid = this.tagDAO.addNewTag(tag);
                    this.postDAO.addPostTagRelation(pid, tid);
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            });
        }
        if(post.getAttachments().size()>0){
            post.getAttachments().stream().forEach(attachment->{
                try {
                    Blob blob = new SerialBlob(attachment.getContent());
                    blob.setBytes(1, attachment.getContent());
                    this.attachmentDAO.addNewAttachment(pid, attachment.getType(), attachment.getName(), attachment.getSize(), blob);
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            });
        }
        return pid;
    }

    @Override
    public void deletePost(int pid) throws SQLException, ClassNotFoundException {
        this.attachmentDAO.deleteAttachment(pid);
        this.postDAO.deletePost(pid);
    }

    @Override
    public int editPost(int pid, Post post) throws SQLException, ClassNotFoundException {
        System.out.println("proceed to edit post");
        this.postDAO.modifyPost(pid, post.getTitle(), post.getContent());
        this.postDAO.deletePTROfPost(pid);
        if(post.getHashTags().size()>0){
            post.getHashTags().stream().forEach(tag->{
                try {
                    int tid = this.tagDAO.addNewTag(tag);
                    this.postDAO.addPostTagRelation(pid, tid);
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            });
        }
        if(post.getAttachments().size()>0){
            post.getAttachments().stream().forEach(attachment->{
                try {
                    Blob blob = new SerialBlob(attachment.getContent());
                    blob.setBytes(1, attachment.getContent());
                    this.attachmentDAO.addNewAttachment(pid, attachment.getType(), attachment.getName(), attachment.getSize(), blob);
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            });
        }
        return pid;
    }

    @Override
    public List<Pair<Post, List<Pair<Integer, String>>>> searchPostByPid(int pid) throws SQLException, ClassNotFoundException{
        return this.postDAO.findByPid(pid);
    }

    @Override
    public List<Pair<Post, List<Pair<Integer, String>>>> searchPostByUser(int userId, int offset, int limit) throws SQLException, ClassNotFoundException {
        return this.postDAO.findByUid(userId, offset, limit);
    }

    @Override
    public List<Pair<Post, List<Pair<Integer, String>>>> searchPostBytime(Time startTime, Time endTime, int offset, int limit) throws SQLException, ClassNotFoundException {
        return this.postDAO.findByTime(startTime, endTime, offset, limit);
    }

    @Override
    public List<Pair<Post, List<Pair<Integer, String>>>> searchPostByTag(int tagId, int offset, int limit) throws SQLException, ClassNotFoundException {
        return this.postDAO.findByHashtag(tagId, offset, limit);
    }

    @Override
    public List<Attachment> downloadAttachement(int aid) throws SQLException, ClassNotFoundException {
        return this.attachmentDAO.getAttachment(aid);
    }

    public int getTotalPostNumber() throws SQLException, ClassNotFoundException {
        return this.postDAO.findTotalNumPost();
    }

    public int getTotalPostNumberByUid(int uid) throws SQLException, ClassNotFoundException {
        return this.postDAO.findTotalNumByUid(uid);
    }

    public int getTotalPostNumberByTime(Time starttime, Time endtime) throws SQLException, ClassNotFoundException {
        return this.postDAO.findTotalNumByTime(starttime, endtime);
    }

    public int getTotalPostNumberByTag(int tid) throws SQLException, ClassNotFoundException {
        return this.postDAO.findTotalNumByHashtag(tid);
    }

    public int editAttchment(int aid, Attachment attachment) throws SQLException, ClassNotFoundException {
        Blob blob = null;
        blob.setBytes(1,attachment.getContent());
        this.attachmentDAO.modifyAttachment(aid, attachment.getType(), attachment.getName(), attachment.getSize(), blob);
        return aid;
    }

    public int deleteAttachment(int aid) throws SQLException, ClassNotFoundException {
        this.attachmentDAO.deleteAttachmentByAid(aid);
        return aid;
    }

    public int findTidByName(String tname) throws SQLException, ClassNotFoundException {
        return this.tagDAO.findTagIdByName(tname);
    }

    public List<Attachment> getAttachmentById(int aid) throws SQLException, ClassNotFoundException {
        return this.attachmentDAO.getAttachment(aid);
    }

}