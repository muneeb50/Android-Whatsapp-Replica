package test.example.com.mychatapp;


public class Users {

    public String name;
    public String image;
    public String status;
    public String thumb_image;

    public String phone;
    public String userid;
    public String Online;

    public String getUserOnlineStatus() {
        return Online;
    }

    public void setUserOnlineStatus(String userOnlineStatus) {
        this.Online = userOnlineStatus;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public Users(){

    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Users(String name, String image, String status, String thumb_image, String phone) {
        this.name = name;
        this.image = image;
        this.status = status;
        this.thumb_image = thumb_image;
        this.phone=phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getThumb_image() {
        return thumb_image;
    }

    public void setThumb_image(String thumb_image) {
        this.thumb_image = thumb_image;
    }

}
