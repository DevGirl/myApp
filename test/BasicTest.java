import java.util.List;
import java.util.Map;

import models.Comment;
import models.Post;
import models.Tag;
import models.User;

import org.junit.Before;
import org.junit.Test;

import play.test.Fixtures;
import play.test.UnitTest;

public class BasicTest extends UnitTest {

    @Before
    public void setup() {
        Fixtures.deleteDatabase();
    }

    @Test
    public void createAndRetriveNewUser() {
        new User("bob@gmail.com", "secret", "Bob").save();
        User user = User.find("byEmail", "bob@gmail.com").first();
        assertNotNull(user);
        assertEquals("Bob", user.fullName);

    }

    @Test
    public void tryConnectAsUser() {
        new User("bob@gmail.com", "secret", "Bob").save();

        assertNotNull(User.connect("bob@gmail.com", "secret"));
        assertNull(User.connect("bob@gmail.com", "badpassword"));
        assertNull(User.connect("tom@gmail.com", "secret"));
    }

    @Test
    public void createPost() {
        User user = new User("bob@gmail.com", "secret", "Bob").save();
        new Post(user, "My first post", "Hello world").save();

        assertEquals(1, Post.count());
        List<Post> bobPosts = Post.find("byAuthor", user).fetch();

        assertEquals(1, bobPosts.size());
        Post firstPost = bobPosts.get(0);
        assertNotNull(firstPost);
        assertEquals(user, firstPost.author);
        assertEquals("My first post", firstPost.title);
        assertEquals("Hello world", firstPost.content);
        assertNotNull(firstPost.postedAt);
    }

    @Test
    public void postComments() {
        User bob = new User("bob@gmail.com", "secret", "Bob").save();

        Post bobPost = new Post(bob, "My first post", "Hello world").save();

        new Comment(bobPost, "Jeff", "Nice post").save();
        new Comment(bobPost, "Tom", "I knew that !").save();

        List<Comment> bobPostComments = Comment.find("byPost", bobPost).fetch();

        assertEquals(2, bobPostComments.size());

        Comment firstComment = bobPostComments.get(0);
        assertNotNull(firstComment);
        assertEquals("Jeff", firstComment.author);
        assertEquals("Nice post", firstComment.content);
        assertNotNull(firstComment.postedAt);

        Comment secondComment = bobPostComments.get(1);
        assertNotNull(secondComment);
        assertEquals("Tom", secondComment.author);
        assertEquals("I knew that !", secondComment.content);
        assertNotNull(secondComment.postedAt);
    }

    @Test
    public void useTheCommentsRelation() {
        User bob = new User("bob@gmail.com", "secret", "Bob").save();

        Post bobPost = new Post(bob, "My first post", "Hello world").save();

        bobPost.addComment("Jeff", "Nice post");
        bobPost.addComment("Tom", "I knew that !");

        assertEquals(1, User.count());
        assertEquals(1, Post.count());
        assertEquals(2, Comment.count());

        bobPost = Post.find("byAuthor", bob).first();
        assertNotNull(bobPost);

        assertEquals(2, bobPost.comments.size());
        assertEquals("Jeff", bobPost.comments.get(0).author);

        bobPost.delete();

        assertEquals(1, User.count());
        assertEquals(0, Post.count());
        assertEquals(0, Comment.count());
    }

    @Test
    public void fullTest() {
        Fixtures.loadModels("data.yml");

        assertEquals(2, User.count());
        assertEquals(3, Post.count());
        assertEquals(3, Comment.count());

        assertNotNull(User.connect("bob@gmail.com", "secret"));
        assertNotNull(User.connect("jeff@gmail.com", "secret"));
        assertNull(User.connect("jeff@gmail.com", "badpassword"));
        assertNull(User.connect("tom@gmail.com", "secret"));

        List<Post> bobPosts = Post.find("author.email", "bob@gmail.com").fetch();
        assertEquals(2, bobPosts.size());

        List<Comment> bobComments = Comment.find("post.author.email", "bob@gmail.com").fetch();
        assertEquals(3, bobComments.size());

        Post frontPost = Post.find("order by postedAt desc").first();
        assertNotNull(frontPost);
        assertEquals("About the model layer", frontPost.title);

        assertEquals(2, frontPost.comments.size());

        frontPost.addComment("Jim", "Hello guys");
        assertEquals(3, frontPost.comments.size());
        assertEquals(4, Comment.count());
    }

    @Test
    public void testTage() {
        User bob = new User("bob@gmail.com", "secret", "Bob").save();
        Post post1 = new Post(bob, "My first post", "Hello world").save();
        Post post2 = new Post(bob, "Hop", "Hello world").save();
        assertEquals(0, Post.findTaggedWith("Red").size());
        
        post1.tagItWith("Red").tagItWith("Blue").save();
        post2.tagItWith("Red").tagItWith("Green").save();
        
        assertEquals(2, Post.findTaggedWith("Red").size());
        assertEquals(1, Post.findTaggedWith("Blue").size());
        assertEquals(1, Post.findTaggedWith("Green").size());
        
        assertEquals(1, Post.findTaggedWith("Red", "Green").size());
        assertEquals(1, Post.findTaggedWith("Red", "Blue").size());
        assertEquals(0, Post.findTaggedWith("Green", "Blue").size());
        assertEquals(0, Post.findTaggedWith("Green", "Blue", "Red").size());
        
        List<Map> cloud = Tag.getCloud();
        assertEquals("[{tag=Blue, pound=1}, {tag=Green, pound=1}, {tag=Red, pound=2}]", cloud.toString());
    }
}
