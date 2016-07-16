package filesearchengineui.view;

import filesearchengineui.view.TestListModel.DocumentWrapper;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

public class DocumentWrapperRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean hasFocus) {
            if (value instanceof DocumentWrapper) {
                return super.getListCellRendererComponent(
                        list, ((DocumentWrapper) value).getName(), index,
                        isSelected, hasFocus);
            }
            return super.getListCellRendererComponent(list, value, index, 
                    isSelected, hasFocus);
        }
    }