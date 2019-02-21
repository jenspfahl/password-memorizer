package de.jepfa.obfusser.ui.credential.list

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filterable
import android.widget.TextView

import de.jepfa.obfusser.R
import de.jepfa.obfusser.model.ObfusChar
import de.jepfa.obfusser.model.ObfusString
import de.jepfa.obfusser.ui.common.CommonMenuFragmentBase
import de.jepfa.obfusser.ui.common.LegendShower
import de.jepfa.obfusser.ui.common.ObfusTextAdjuster
import de.jepfa.obfusser.ui.credential.input.CredentialInputNameActivity

class CredentialIntroFragment : CommonMenuFragmentBase() {
    override fun refresh() {
        refreshMenuLockItem()
    }

    override fun getMenuId(): Int {
        return R.menu.toolbar_menu_intro
    }

    override fun getFilterable(): Filterable? {
        return null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.navtab_credential_intro, container, false)
        activity!!.setTitle(R.string.title_credentials)

        val textViewLegend : TextView = view.findViewById(R.id.credential_intro_legend)
        val legend = LegendShower.buildLegend(activity, secureActivity.patternRepresentation)
        textViewLegend.setText(legend)

        val textViewPasswd : TextView = view.findViewById(R.id.credential_intro_text_passwd)
        val textViewObfus : TextView = view.findViewById(R.id.credential_intro_text_obfus)
        textViewObfus.setText(ObfusString.obfuscate(
                textViewPasswd.text.toString()).toRepresentation(secureActivity.patternRepresentation))

        val fab : FloatingActionButton = view.findViewById(R.id.fab)
        fab.setOnClickListener(View.OnClickListener { view ->
            val context = view.context
            val intent = Intent(context, CredentialInputNameActivity::class.java)
            startActivity(intent)
        })
        // Inflate the layout for this fragment
        return view
    }

}
