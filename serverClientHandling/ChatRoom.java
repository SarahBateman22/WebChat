import java.util.ArrayList;
import java.util.List;

public class ChatRoom {
    private List<MyRunnable> clientArr_ = new ArrayList<>();
    String name_;

    ChatRoom(String name){
        name_=name;
    }
    public synchronized void helperSend(String message) {
        for (MyRunnable client : clientArr_) {
            client.encodeMessage(message);
        }
    }
    public synchronized void addClient(MyRunnable runnable) {
        for(MyRunnable client : clientArr_){
            runnable.encodeMessage(MyRunnable.makeJoinMsg(client.getRoomName_(), client.getUsername_()));
        }
        clientArr_.add(runnable);
        helperSend(MyRunnable.makeJoinMsg(runnable.getRoomName_(), runnable.getUsername_()));

    }

    public synchronized void removeClient(MyRunnable runnable) {
        clientArr_.remove(runnable);
        helperSend(MyRunnable.makeLeaveMsg(runnable.getRoomName_(), runnable.getUsername_()));
    }

    public synchronized void sendMessage(MyRunnable runnable, String msg){
        helperSend(MyRunnable.makeMsgMsg(runnable.getRoomName_(), runnable.getUsername_(), msg));
    }

}
