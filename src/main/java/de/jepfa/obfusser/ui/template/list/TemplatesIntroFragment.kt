package de.jepfa.obfusser.ui.template.list

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filterable
import de.jepfa.obfusser.R
import de.jepfa.obfusser.ui.common.CommonMenuFragmentBase
import de.jepfa.obfusser.ui.template.input.TemplateInputNameActivity

class TemplatesIntroFragment : CommonMenuFragmentBase() {
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

        val view = inflater.inflate(R.layout.navtab_template_intro, container, false)
        activity!!.setTitle(R.string.title_templates)

        val fab : FloatingActionButton = view.findViewById(R.id.fab)
        fab.setOnClickListener(View.OnClickListener { view ->
            val context = view.context
            val intent = Intent(context, TemplateInputNameActivity::class.java)
            startActivity(intent)
        })

        return view
    }

}
