/*
 * Copyright (C) 2019 European Spallation Source ERIC.
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */

package org.phoebus.logbook.olog.ui.write;

import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.phoebus.framework.nls.NLS;
import org.phoebus.logbook.olog.ui.AttachmentsPreviewController;
import org.phoebus.logbook.olog.ui.Messages;

import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LogEntryEditorStage extends Stage
{

    /**
     * A stand-alone window containing components needed to create a logbook entry.
     * @param parent The {@link Node} from which the user - through context menu or application menu - requests a new
     *               logbook entry.
     * @param logEntryModel Pre-populated data for the log entry, e.g. date and (optionally) screen shot.
     * @param completionHandler If non-null, called when the submission to the logbook service has completed.
     */
    public LogEntryEditorStage(Node parent, LogEntryModel logEntryModel, LogEntryCompletionHandler completionHandler)
    {
        initModality(Modality.WINDOW_MODAL);
        ResourceBundle resourceBundle =  NLS.getMessages(Messages.class);
        FXMLLoader fxmlLoader =
                new FXMLLoader(getClass().getResource("LogEntryEditor.fxml"), resourceBundle);
        fxmlLoader.setControllerFactory(clazz -> {
            try {
                if(clazz.isAssignableFrom(LogEntryEditorController.class)){
                    return clazz.getConstructor(Node.class, LogEntryModel.class, LogEntryCompletionHandler.class)
                            .newInstance(parent, logEntryModel, completionHandler);
                }
                else if(clazz.isAssignableFrom(FieldsViewController.class)){
                    return clazz.getConstructor(LogEntryModel.class)
                            .newInstance(logEntryModel);
                }
                else if(clazz.isAssignableFrom(AttachmentsViewController.class)){
                    return clazz.getConstructor(LogEntryModel.class, Boolean.class)
                                    .newInstance(logEntryModel, true);
                }
                else if(clazz.isAssignableFrom(AttachmentsPreviewController.class)){
                    return clazz.getConstructor().newInstance();
                }
                else if(clazz.isAssignableFrom(LogPropertiesEditorController.class)) {
                    return clazz.getConstructor(LogEntryModel.class).newInstance(logEntryModel);
                }
            } catch (Exception e) {
                Logger.getLogger(LogEntryEditorStage.class.getName()).log(Level.SEVERE, "Failed to construct controller for log editor UI", e);
            }
            return null;
        });

        try {
            fxmlLoader.load();
        } catch (
                Exception exception) {
            Logger.getLogger(LogEntryEditorStage.class.getName()).log(Level.WARNING, "Unable to load fxml for log entry editor UI", exception);
        }

        Scene scene = new Scene(fxmlLoader.getRoot());
        setScene(scene);
    }
}
