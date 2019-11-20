package de.jepfa.obfusser.ui.common

import android.content.Context
import android.content.DialogInterface
import android.support.v7.app.AlertDialog

import de.jepfa.obfusser.R
import de.jepfa.obfusser.model.Credential
import de.jepfa.obfusser.model.Group
import de.jepfa.obfusser.model.Template
import de.jepfa.obfusser.repository.credential.CredentialRepository
import de.jepfa.obfusser.repository.group.GroupRepository
import de.jepfa.obfusser.repository.template.TemplateRepository

object DeletionHelper {

    fun askAndDelete(repository: CredentialRepository,
                     credential: Credential, context: Context, r: Runnable?) {
        AlertDialog.Builder(context)
                .setTitle(R.string.title_delete_credential)
                .setMessage(context.getString(R.string.message_delete_credential, credential.name))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes) { dialog, whichButton ->
                    repository.delete(credential)
                    r?.run()
                }
                .setNegativeButton(android.R.string.no, null)
                .show()
    }

    fun askAndDelete(repository: TemplateRepository,
                     template: Template, context: Context, r: Runnable?) {
        AlertDialog.Builder(context)
                .setTitle(R.string.title_delete_template)
                .setMessage(context.getString(R.string.message_delete_template, template.name))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes) { dialog, whichButton ->
                    repository.delete(template)
                    r?.run()
                }
                .setNegativeButton(android.R.string.no, null)
                .show()
    }

    fun askAndDelete(repository: GroupRepository,
                     group: Group, context: Context, r: Runnable?) {
        AlertDialog.Builder(context)
                .setTitle(R.string.title_delete_group)
                .setMessage(context.getString(R.string.message_delete_group, group.name))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes) { dialog, whichButton ->
                    repository.delete(group)
                    r?.run()
                }
                .setNegativeButton(android.R.string.no, null)
                .show()
    }
}
