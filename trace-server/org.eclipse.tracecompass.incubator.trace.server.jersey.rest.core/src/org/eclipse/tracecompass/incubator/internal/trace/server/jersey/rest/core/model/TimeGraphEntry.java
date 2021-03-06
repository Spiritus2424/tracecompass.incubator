/**********************************************************************
 * Copyright (c) 2021, 2022 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.model;

import java.util.Collection;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Contributes to the model used for TSP swagger-core annotations.
 */
@Schema(allOf = TreeDataModel.class)
public interface TimeGraphEntry {

    /**
     * @return The start time.
     */
    @Schema(description = "Beginning of the range for which this entry exists")
    long getStart();

    /**
     * @return The end time.
     */
    @Schema(description = "End of the range for which this entry exists")
    long getEnd();

    /**
     * @return The entry's metadata map.
     */
    @Schema(description = "Optional metadata map for domain specific data for matching data across data providers. Keys for the same data shall be the same across data providers. "
            + "Only values of type Number or String are allowed. For each key all values shall have the same type.")
    Map<String, Collection<Object>> getMetadata();
}
