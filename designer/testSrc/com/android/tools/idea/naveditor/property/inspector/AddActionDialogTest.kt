// Copyright (C) 2017 The Android Open Source Project
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.android.tools.idea.naveditor.property.inspector

import com.android.SdkConstants.AUTO_URI
import com.android.tools.idea.naveditor.NavModelBuilderUtil.navigation
import com.android.tools.idea.naveditor.NavTestCase
import org.jetbrains.android.dom.navigation.NavigationSchema

class AddActionDialogTest : NavTestCase() {
  fun testExisting() {
    val model = model("nav.xml") {
      navigation {
        fragment("f1") {
          action("a1", destination = "f2") {
            withAttribute(AUTO_URI, NavigationSchema.ATTR_ENTER_ANIM, "@anim/fade_in")
            withAttribute(AUTO_URI, NavigationSchema.ATTR_POP_UP_TO, "@id/f2")
            withAttribute(AUTO_URI, NavigationSchema.ATTR_CLEAR_TASK, "true")
          }
        }
        fragment("f2")
      }
    }

    val dialog = AddActionDialog(AddActionDialog.Defaults.NORMAL, model.find("a1"), model.find("f1")!!, null)
    dialog.close(0)
    assertEquals(model.find("f2"), dialog.destination)
    assertEquals("@anim/fade_in", dialog.enterTransition)
    assertTrue(dialog.isClearTask)
    assertEquals(model.find("f1"), dialog.source)
    assertEquals("@id/f2", dialog.popTo)
  }

  fun testContent() {
    val model = model("nav.xml") {
      navigation("root") {
        fragment("f1")
        fragment("f2")
      }
    }

    val dialog = AddActionDialog(AddActionDialog.Defaults.NORMAL, null, model.find("f1")!!, null)
    dialog.myDestinationComboBox.selectedIndex = 1

    assertEquals(model.find("f1"), dialog.myFromComboBox.getItemAt(0))
    assertEquals(1, dialog.myFromComboBox.itemCount)
    assertFalse(dialog.myFromComboBox.isEnabled)

    assertEquals(null, dialog.myDestinationComboBox.getItemAt(0))
    assertEquals(model.find("f1"), dialog.myDestinationComboBox.getItemAt(1))
    assertEquals(model.find("f2"), dialog.myDestinationComboBox.getItemAt(2))
    assertEquals(model.find("root"), dialog.myDestinationComboBox.getItemAt(3))
    assertEquals(4, dialog.myDestinationComboBox.itemCount)
    assertTrue(dialog.myDestinationComboBox.isEnabled)

    assertEquals(null, dialog.myEnterComboBox.getItemAt(0).value)
    assertEquals("@anim/fade_in", dialog.myEnterComboBox.getItemAt(1).value)
    assertEquals("@anim/fade_out", dialog.myEnterComboBox.getItemAt(2).value)
    assertEquals("@animator/test1", dialog.myEnterComboBox.getItemAt(3).value)
    assertEquals(4, dialog.myEnterComboBox.itemCount)

    assertEquals(null, dialog.myExitComboBox.getItemAt(0).value)
    assertEquals("@anim/fade_in", dialog.myExitComboBox.getItemAt(1).value)
    assertEquals("@anim/fade_out", dialog.myExitComboBox.getItemAt(2).value)
    assertEquals("@animator/test1", dialog.myExitComboBox.getItemAt(3).value)
    assertEquals(4, dialog.myExitComboBox.itemCount)

    assertEquals(null, dialog.myPopToComboBox.getItemAt(0).value)
    assertEquals("@id/root", dialog.myPopToComboBox.getItemAt(1).value)
    assertEquals("@id/f1", dialog.myPopToComboBox.getItemAt(2).value)
    assertEquals("@id/f2", dialog.myPopToComboBox.getItemAt(3).value)
    assertEquals(4, dialog.myPopToComboBox.itemCount)

    dialog.close(0)
  }

  fun testDefaults() {
    val model = model("nav.xml") {
      navigation("root") {
        fragment("f1")
        fragment("f2")
      }
    }

    val f1 = model.find("f1")!!

    var dialog = AddActionDialog(AddActionDialog.Defaults.NORMAL, null, f1, null)
    assertEquals(null, dialog.destination)
    assertEquals(f1, dialog.source)
    assertFalse(dialog.isInclusive)
    assertEquals(null, dialog.popTo)
    dialog.close(0)

    dialog = AddActionDialog(AddActionDialog.Defaults.GLOBAL, null, f1, null)
    assertEquals(f1, dialog.destination)
    assertEquals(model.find("root"), dialog.source)
    assertFalse(dialog.isInclusive)
    assertEquals(null, dialog.popTo)
    dialog.close(0)

    dialog = AddActionDialog(AddActionDialog.Defaults.RETURN_TO_SOURCE, null, f1, null)
    assertEquals(null, dialog.destination)
    assertEquals(f1, dialog.source)
    assertTrue(dialog.isInclusive)
    assertEquals("@id/f1", dialog.popTo)
    dialog.close(0)
  }
}