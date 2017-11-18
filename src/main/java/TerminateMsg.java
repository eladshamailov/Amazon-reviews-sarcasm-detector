public class TerminateMsg extends Msg{

    public TerminateMsg() {
        super.action=Actions.TERMINATE.ordinal();
    }

    public int getAction() {
        return super.action;
    }

    public void setAction(int action) {
        super.action = action;
    }
}
