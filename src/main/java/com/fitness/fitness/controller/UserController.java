package com.fitness.fitness.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.fitness.fitness.model.User;
import com.fitness.fitness.service.UserService;

import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.multipart.MultipartFile;

@Controller
@SessionAttributes("user")
public class UserController {
    
    
    private UserService userService;
    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    @GetMapping("/first_page")
    public String firstPage(Model model){

        return "firstpage";
    }
    @GetMapping("/register_user")
    public String getNewUserPage(Model model){
        model.addAttribute("user", new User());
        return "registeruserform";
    }

    @PostMapping("/register_user")
    public String registerUser(@ModelAttribute User user, HttpSession session) {
        if (userService.verifyUser(user)) {
            if(userService.emailValid(user.getEmail()) && userService.passwordValid(user.getPassword())){
                userService.saveUser(user);
                session.setAttribute("user", user);
                return "home";
            }
        }
        return "registeruserform";
    }
    
    @GetMapping("/user_signin")
    public String showLoginForm(Model model) {
        User existingUser = new User();
        model.addAttribute("user", existingUser ) ;
        return "sign";
    }

    @PostMapping("/user_signin")
    public String login(@ModelAttribute User user, HttpSession session) {
        if (userService.userLogin(user)) {
            session.setAttribute("user", user);
            return "home";
        }
        return "login_fail";
    }

    @GetMapping("/forget_password")
    public String userForgetPassword(Model model, User user){ // existinguser masuk ke model dgn nama "user"
        return "forgetPassword";
    }
    
 
    @PostMapping("/forget_password")
    public String processPasswordReset(Model model, @ModelAttribute User user) {
        User retrievedUser = userService.getUserByEmail(user.getEmail());
        if (retrievedUser == null) {
            model.addAttribute("error", "No user found with the provided email");
            return "forgetPassword";
        }
        
        if (!userService.userRecoveryCode(retrievedUser)) {
            model.addAttribute("error", "Invalid recovery code");
            return "forgetPassword";
        }
    
        if (userService.isNewPasswordDifferent(user, retrievedUser)) {
            model.addAttribute("error", "The new password must be different from the current password");
            return "forgetPassword";
        }

        if (!userService.passwordValid(user.getPassword())) {
            model.addAttribute("error", "The new password must be at least 8 characters long and include at least one uppercase letter and one number");
            return "forgetPassword";
        }
    
        // Update password and clear recovery code
        retrievedUser.setPassword(user.getPassword());
        userService.saveUser(retrievedUser);
    
        return "sign"; // Redirect to the login page or confirmation page
    }
    @GetMapping("/profile")
    public String userProfile(Model model, @SessionAttribute("user") User user) { // user in User user hold attribute from session attribute "user" atau itu masuk ke User user
        User retrievedUser = userService.getUserByEmail(user.getEmail());
        if (retrievedUser != null) {
            model.addAttribute("user", retrievedUser);
        return "profile";
            } else {
            model.addAttribute("error", "User not found");
        return "profile";
        }
    }

    @GetMapping("/edit_profile")
    public String userEditInformation(Model model, @SessionAttribute("user") User user) {
        User existingUser = userService.getUserByEmail(user.getEmail());
        model.addAttribute("user", existingUser);
        return "editProfile";
    }

    @PostMapping("/edit_profile")
    public String userEditInformation(@ModelAttribute User user, Model model) {
        userService.saveUser(user);
        return "editProfile";
}

    @GetMapping("/add_credit_card")
    public String addCreditCard(Model model,User user) {
        User u = userService.getUserByEmail(user.getEmail());
        model.addAttribute("user", u);
        return "profile";
    }
    
    @PostMapping("/add_credit_card")
    public String userAddCreditCard(@ModelAttribute User user, Model model) {
        String cardNumber = user.getCardNumber();
        userService.setCreditCardNumber(user.getEmail(), cardNumber);
        return "profile"; 
    }
    @GetMapping("/home_page")
    public String getHomePage(Model model, @SessionAttribute("user") User user) {
        if (user != null) {
            model.addAttribute("user", user);
            return "home";
        } else {
            return "login";
        }
}
@GetMapping("/reset_password")
public String showResetPasswordForm(Model model, HttpSession session) {
    User user = (User) session.getAttribute("user");
    if (user == null) {
        return "/user_signin";
    }
    model.addAttribute("user", user);
    return "resetPassword";
}

@PostMapping("/reset_password")
public String resetPassword(@RequestParam("currentPassword") String password,
                            @RequestParam("newPassword") String newPassword,
                            @RequestParam("confirmPassword") String confirmPassword,
                            HttpSession session, Model model) {
    User user = (User) session.getAttribute("user");
    if (user == null) {
        return "redirect:/user_signin";
    }

    if (!newPassword.equals(confirmPassword)) {
        model.addAttribute("error", "New password and confirmation do not match.");
        return "resetPassword";
    }
    if (password.equals(newPassword)) {
        model.addAttribute("error", "New password cannot be the same as the current password.");
        return "resetPassword";
    }

    if (userService.resetPassword(user.getEmail(), password, newPassword)) {
        session.invalidate(); 
        return "redirect:/user_signin";
    } else {
        model.addAttribute("error", "Password reset failed. Please check your current password.");
        return "resetPassword";
    }
}

@GetMapping("/choose_profile_picture")
public String chooseProfilePicture(Model model) {
    model.addAttribute("avatar2", "avatar2.jpg");
    model.addAttribute("avatar3", "avatar3.jpg");
    return "profilePicture";
}

@PostMapping("/save_profile_picture")
public String saveProfilePicture(@RequestParam("avatar") String avatar, HttpSession session) {
    User user = (User) session.getAttribute("user");
    if (user != null) {
        user.setImage(avatar);
        userService.saveUserProfile(user);
    }
    return "editProfile";
}

@PostMapping("/remove_profile_picture")
public String removeProfilePicture(HttpSession session) {
    User user = (User) session.getAttribute("user");
    if (user != null) {
        user.setImage("avatar1.jpg");
        userService.saveUserProfile(user);
    }
    return "editProfile";
}

}
