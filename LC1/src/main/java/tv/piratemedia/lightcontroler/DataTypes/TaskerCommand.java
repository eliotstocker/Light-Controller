package tv.piratemedia.lightcontroler.DataTypes;

/**
 * Created by DudeOfAwesome on 10/20/14.
 */
public class TaskerCommand {
    public enum TASKTYPE {ON, OFF, WHITE, COLOR, BRIGHTNESS};
    public TASKTYPE task = TASKTYPE.ON;
    public int data = -1;
    public int zone = 0;

    public TaskerCommand () {

    }

    public TaskerCommand (String in) {
        String[] split = in.split(";");
        zone = Integer.parseInt(split[0]);
        task = TASKTYPE.values()[Integer.parseInt(split[1])];
        data = Integer.parseInt(split[2]);
    }

    public TaskerCommand (int zone, TASKTYPE task) {
        this.zone = zone;
        this.task = task;
    }

    public TaskerCommand (int zone, TASKTYPE task, int data) {
        this.zone = zone;
        this.task = task;
        this.data = data;
    }

    public String toString () {
        return zone + ";" + task.ordinal() + ";" + data + ";" + "";
    }
}
