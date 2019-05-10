package be.kuleuven.softdev.blendfangnan.timers;

public interface ItemTouchHelperAdapter {

    /**
     *
     * Use in case long press move is enabled!
     *
     * @param fromPosition
     * @param toPosition
     */

    void onItemMove (int fromPosition, int toPosition);

    void onItemDismiss (int position);
}
