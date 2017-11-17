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

import com.android.SdkConstants.AUTO_URI
import com.android.tools.idea.naveditor.NavModelBuilderUtil
import com.android.tools.idea.naveditor.NavigationTestCase
import org.jetbrains.android.dom.navigation.NavigationSchema

class SetStartDestinationPropertyTest : NavigationTestCase() {

  fun testProperty() {
    val model = model("nav.xml",
        NavModelBuilderUtil.rootComponent("root")
            .withStartDestinationAttribute("f1")
            .unboundedChildren(
                NavModelBuilderUtil.fragmentComponent("f1"),
                NavModelBuilderUtil.fragmentComponent("f2"),
                NavModelBuilderUtil.navigationComponent("subnav")
                    .withStartDestinationAttribute("activity")
                    .unboundedChildren(
                        NavModelBuilderUtil.fragmentComponent("f3"),
                        NavModelBuilderUtil.activityComponent("activity"))))
        .build()

    var property = SetStartDestinationProperty(listOf(model.find("f1")!!))
    assertNotNull(property.value)
    property = SetStartDestinationProperty(listOf(model.find("f2")!!))
    assertNull(property.value)
    property = SetStartDestinationProperty(listOf(model.find("subnav")!!))
    assertNull(property.value)

    property = SetStartDestinationProperty(listOf(model.find("f3")!!))
    assertNull(property.value)
    property = SetStartDestinationProperty(listOf(model.find("activity")!!))
    assertNotNull(property.value)

    property = SetStartDestinationProperty(listOf(model.find("f2")!!))
    property.setValue("true")
    assertEquals("@id/f2", model.find("root")!!.getAttribute(AUTO_URI, NavigationSchema.ATTR_START_DESTINATION))
    assertNotNull(property.value)
    property = SetStartDestinationProperty(listOf(model.find("f1")!!))
    assertNull(property.value)

    property = SetStartDestinationProperty(listOf(model.find("f3")!!))
    property.setValue("true")
    assertEquals("@id/f3", model.find("subnav")!!.getAttribute(AUTO_URI, NavigationSchema.ATTR_START_DESTINATION))
    assertEquals("@id/f2", model.find("root")!!.getAttribute(AUTO_URI, NavigationSchema.ATTR_START_DESTINATION))
    assertNotNull(property.value)
    property = SetStartDestinationProperty(listOf(model.find("activity")!!))
    assertNull(property.value)
  }
}