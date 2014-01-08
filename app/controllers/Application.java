package controllers;

import java.util.List;

import models.Post;
import play.Play;
import play.cache.Cache;
import play.data.validation.Required;
import play.libs.Codec;
import play.libs.Images;
import play.mvc.Before;
import play.mvc.Controller;

public class Application extends Controller {

	@Before
	static void addDefaults() {
		renderArgs.put("blogTitle", Play.configuration.getProperty("blog.title"));
		renderArgs.put("blogBaseline", Play.configuration.getProperty("blog.baseline"));
	}

	public static void index() {
		Post post = Post.find("order by postedAt desc").first();
		List<Post> oPosts = Post.find("order by postedAt desc").from(1).fetch(10);
		render(post, oPosts);
	}

	public static void show(Long id) {
		Post post = Post.findById(id);
		String randomId = Codec.UUID();
		render(post, randomId);
	}

	public static void postComment(Long postId, @Required(message = "Author is required") String author,
			@Required(message = "A message is required") String content, @Required(message = "Please type the code") String code, String randomId) {
		Post post = Post.findById(postId);
		validation.equals(code, Cache.get(randomId)).message("Invalid code. Please type it again");
		if (validation.hasErrors()) {
			render("Application/show.html", post);
		}
		post.addComment(author, content);
		flash.success("Thanks for posting %s", author);
		Cache.delete(randomId);
		show(postId);
	}

	public static void captcha(String id) {
		Images.Captcha captcha = Images.captcha();
		String code = captcha.getText("#E4EAFD");
		Cache.set(id, code, "10mn");
		renderBinary(captcha);
	}

}