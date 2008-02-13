/**
 *  Copyright (C) 2006 Orbeon, Inc.
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms of the
 *  GNU Lesser General Public License as published by the Free Software Foundation; either version
 *  2.1 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Lesser General Public License for more details.
 *
 *  The full text of the license is available at http://www.gnu.org/copyleft/lesser.html
 */
package org.orbeon.oxf.xforms.action;

import org.dom4j.Element;
import org.orbeon.oxf.pipeline.api.PipelineContext;
import org.orbeon.oxf.xforms.event.XFormsEventHandlerContainer;
import org.orbeon.saxon.om.Item;

/**
 * Base class for all actions.
 */
public abstract class XFormsAction {
    public abstract void execute(XFormsActionInterpreter actionInterpreter, PipelineContext pipelineContext,
                                 String targetId, XFormsEventHandlerContainer eventHandlerContainer, Element actionElement,
                                 boolean hasOverriddenContext, Item overriddenContext);
}
