package sara.damien.app.chat;

/**
 * Created by Sara-Fleur on 2/27/14.
 */
public class Message {
    String message;
    boolean isMine;
    /**
     * boolean to determine, whether the message is a status message or not.
     * it reflects the changes/updates about the sender is writing, have entered text etc
     */
    boolean isStatusMessage;
    boolean isSent;
    String time;

    public Message(String message, boolean isMine, boolean isSent,String time) {
        super();
        this.message = message;
        this.isMine = isMine;
        this.isStatusMessage = false;
        this.isSent=isSent;
        this.time=time;
    }
    /**
     * Constructor to make a status Message object
     * consider the parameters are swaped from default Message constructor,
     *  not a good approach but have to go with it.
     */
    public Message(boolean status, String message) {
        super();
        this.message = message;
        this.isMine = false;
        this.isStatusMessage = status;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public boolean isMine() {
        return isMine;
    }
    public void setMine(boolean isMine) {
        this.isMine = isMine;
    }
    public boolean isStatusMessage() {
        return isStatusMessage;
    }
    public void setStatusMessage(boolean isStatusMessage) {
        this.isStatusMessage = isStatusMessage;
    }
    public boolean isSent(){return this.isSent;}
    public void setSent(){this.isSent=true;}
    public String getTime(){return this.time;}
}
