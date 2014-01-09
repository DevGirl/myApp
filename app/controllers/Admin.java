package controllers;

import java.util.List;

import models.Post;
import models.User;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

@With(Security.class)
public class Admin extends Controller {
    
    @Before
    static void setConnecteUser(){
        if(Security.isConnected()){
            User user = User.find("byEmail", Security.connected()).first();
            renderArgs.put("user", user.fullName);
        }
    }
    
    public static void index(){
        String user = Security.connected();
        List<Post> posts = Post.find("author.email", user).fetch();
        render(posts);
    }

   public static void form(){
        render();
   }

   public static void save(){
   }
}
