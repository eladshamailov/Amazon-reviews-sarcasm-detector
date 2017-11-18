public class TerminateMsg {
    int action;

    public TerminateMsg() {
        action=Actions.TERMINATE.ordinal();
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }
}
