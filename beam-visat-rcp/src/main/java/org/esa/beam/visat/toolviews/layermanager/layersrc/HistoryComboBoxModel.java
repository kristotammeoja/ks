package org.esa.beam.visat.toolviews.layermanager.layersrc;

import org.esa.beam.framework.ui.UserInputHistory;

import javax.swing.ComboBoxModel;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;


public class HistoryComboBoxModel implements ComboBoxModel {

    private final UserInputHistory history;
    private EventListenerList listenerList;


    public HistoryComboBoxModel(UserInputHistory history) {
        this.history = history;
        listenerList = new EventListenerList();
    }

    public UserInputHistory getHistory() {
        return history;
    }

    @Override
    public void setSelectedItem(Object anObject) {
        if (anObject instanceof String) {
            history.push((String) anObject);
            fireContentChanged();
        }
    }

    private void fireContentChanged() {
        final ListDataListener[] listDataListeners = listenerList.getListeners(ListDataListener.class);
        for (ListDataListener listener : listDataListeners) {
            listener.contentsChanged(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, 0));
        }
    }

    @Override
    public Object getSelectedItem() {
        final String[] entries = history.getEntries();
        if (entries == null) {
            return null;
        }
        return entries[0];
    }

    @Override
    public int getSize() {
        return history.getNumEntries();

    }

    @Override
    public Object getElementAt(int index) {
        return history.getEntries()[index];
    }

    @Override
    public void addListDataListener(ListDataListener listener) {
        listenerList.add(ListDataListener.class, listener);
    }

    @Override
    public void removeListDataListener(ListDataListener listener) {
        listenerList.remove(ListDataListener.class, listener);
    }
}
