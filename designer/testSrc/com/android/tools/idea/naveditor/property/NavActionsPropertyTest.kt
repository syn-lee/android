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
package com.android.tools.idea.naveditor.property

import com.android.tools.idea.common.SyncNlModel
import com.android.tools.idea.naveditor.NavModelBuilderUtil.navigation
import com.android.tools.idea.naveditor.NavTestCase

class NavActionsPropertyTest : NavTestCase() {
  private lateinit var model: SyncNlModel

  override fun setUp() {
    super.setUp()
    model = model("nav.xml") {
      navigation {
        fragment("f1") {
          action("a1", destination = "f2")
          action("a2", destination = "f3")
        }
        fragment("f2")
        fragment("f3")
      }
    }
  }

  fun testMultipleActions() {
    val property = NavActionsProperty(listOf(model.find("f1")!!))
    assertEquals(model.find("a1"), property.getChildProperty("f2").components[0])
    assertEquals(model.find("a2"), property.getChildProperty("f3").components[0])
  }

  fun testNoActions() {
    val property = NavActionsProperty(listOf(model.find("f2")!!))
    assertTrue(property.properties.isEmpty())
  }

  fun testModify() {
    val fragment = model.find("f2")!!
    val property = NavActionsProperty(listOf(fragment))
    val action = model.find("a1")!!
    fragment.addChild(action)
    property.refreshList()
    assertEquals(action, property.getChildProperty("f2").components[0])
    fragment.removeChild(action)
    property.refreshList()
    assertTrue(property.properties.isEmpty())
  }
}