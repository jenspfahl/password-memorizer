package de.jepfa.obfusser.ui.common;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import de.jepfa.obfusser.model.Credential;
import de.jepfa.obfusser.model.Group;
import de.jepfa.obfusser.model.Template;
import de.jepfa.obfusser.repository.credential.CredentialRepository;
import de.jepfa.obfusser.repository.group.GroupRepository;
import de.jepfa.obfusser.repository.template.TemplateRepository;

public class DeletionHelper {

    public static void askAndDelete(final CredentialRepository repository,
                                       final Credential credential, Context context, final Runnable r) {
        new AlertDialog.Builder(context)
                .setTitle("Delete credential")
                .setMessage("Do you really want to delete this credential with name '" + credential.getName()+ "' ?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        repository.delete(credential);
                        if (r != null) {
                            r.run();
                        }
                    }})
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    public static void askAndDelete(final TemplateRepository repository,
                                    final Template template, Context context, final Runnable r) {
        new AlertDialog.Builder(context)
                .setTitle("Delete template")
                .setMessage("Do you really want to delete this template with name '" + template.getName()+ "' ?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        repository.delete(template);
                        if (r != null) {
                            r.run();
                        }
                    }})
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    public static void askAndDelete(final GroupRepository repository,
                                    final Group group, Context context, final Runnable r) {
        new AlertDialog.Builder(context)
                .setTitle("Delete group")
                .setMessage("Do you really want to delete this group with name '" + group.getName()+ "' ?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        repository.delete(group);
                        if (r != null) {
                            r.run();
                        }
                    }})
                .setNegativeButton(android.R.string.no, null)
                .show();
    }
}
