/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.tools.idea.naveditor.property.inspector

import com.android.tools.idea.common.model.NlComponent
import com.android.tools.idea.common.property.NlProperty
import com.android.tools.idea.common.property.editors.NlComponentEditor
import com.android.tools.idea.common.property.inspector.InspectorComponent
import com.android.tools.idea.common.property.inspector.InspectorPanel
import com.android.tools.idea.common.property.inspector.InspectorProvider
import com.android.tools.idea.naveditor.property.NavDestinationArgumentsProperty
import com.android.tools.idea.naveditor.property.NavPropertiesManager
import com.android.tools.idea.naveditor.property.editors.TextEditor
import com.android.tools.idea.naveditor.surface.NavDesignSurface
import com.android.tools.idea.uibuilder.property.editors.NlEditingListener
import com.android.tools.idea.uibuilder.property.editors.NlTableCellEditor
import com.intellij.ui.components.JBTextField
import com.intellij.ui.table.JBTable
import java.awt.BorderLayout
import javax.swing.BorderFactory
import javax.swing.JPanel
import javax.swing.table.TableCellRenderer

const val NAV_ARGUMENTS_COMPONENT_NAME = "NavArgumentsPropertyInspector"

class NavDestinationArgumentsInspectorProvider : InspectorProvider<NavPropertiesManager> {

  private var inspector: NavArgumentsInspectorComponent? = null

  override fun isApplicable(components: List<NlComponent>,
                            properties: Map<String, NlProperty>,
                            propertiesManager: NavPropertiesManager): Boolean {
    if (components.size != 1) {
      return false
    }
    if (properties.values.none { it is NavDestinationArgumentsProperty }) {
      return false
    }

    return true
  }

  override fun createCustomInspector(components: List<NlComponent>,
                                     properties: Map<String, NlProperty>,
                                     propertiesManager: NavPropertiesManager): InspectorComponent<NavPropertiesManager> {
    val inspector = inspector ?: NavArgumentsInspectorComponent()
    this.inspector = inspector

    inspector.updateProperties(components, properties, propertiesManager)
    return inspector
  }

  override fun resetCache() {
    inspector = null
  }

  private class NavArgumentsInspectorComponent :
      InspectorComponent<NavPropertiesManager> {

    private lateinit var argumentProperty: NavDestinationArgumentsProperty
    private val components = mutableListOf<NlComponent>()
    private var surface: NavDesignSurface? = null

    override fun updateProperties(components: List<NlComponent>,
                                  properties: Map<String, NlProperty>,
                                  propertiesManager: NavPropertiesManager) {
      this.components.clear()
      this.components.addAll(components)

      surface = propertiesManager.designSurface as? NavDesignSurface

      argumentProperty = properties.values.filterIsInstance(NavDestinationArgumentsProperty::class.java).first()
      refresh()
    }

    override fun getEditors(): List<NlComponentEditor> = listOf()

    override fun getMaxNumberOfRows() = 2

    override fun attachToInspector(inspector: InspectorPanel<NavPropertiesManager>) {
      val panel = JPanel(BorderLayout())
      val table = JBTable(NavArgumentsTableModel(argumentProperty))
      table.name = NAV_ARGUMENTS_COMPONENT_NAME

      val nameCellRenderer = JBTextField()
      nameCellRenderer.emptyText.text = "name"
      nameCellRenderer.border = BorderFactory.createEmptyBorder()
      table.columnModel.getColumn(0).cellRenderer = TableCellRenderer { _, value, _, _, _, _ ->
        nameCellRenderer.also { it.text = (value as? NlProperty)?.value }
      }

      val defaultValueCellRenderer = JBTextField()
      defaultValueCellRenderer.emptyText.text = "default value"
      defaultValueCellRenderer.border = BorderFactory.createEmptyBorder()
      table.columnModel.getColumn(1).cellRenderer = TableCellRenderer { _, value, _, _, _, _ ->
        defaultValueCellRenderer.also { it.text = (value as? NlProperty)?.value }
      }

      val nameTextEditor = TextEditor(surface!!.project, NlEditingListener.DEFAULT_LISTENER)
      val defaultValueTextEditor = TextEditor(surface!!.project, NlEditingListener.DEFAULT_LISTENER)

      val nameEditor = NlTableCellEditor()
      nameEditor.init(nameTextEditor, null)
      val defaultValueEditor = NlTableCellEditor()
      defaultValueEditor.init(defaultValueTextEditor, null)

      table.columnModel.getColumn(0).cellEditor = nameEditor
      table.columnModel.getColumn(1).cellEditor = defaultValueEditor

      table.putClientProperty("terminateEditOnFocusLost", true)

      panel.add(table, BorderLayout.CENTER)
      val emptyPanel = JPanel()
      inspector.addExpandableComponent("Arguments", null, emptyPanel, emptyPanel)
      inspector.addPanel(panel)
    }

    override fun refresh() {
      argumentProperty.refreshList()
    }
  }
}