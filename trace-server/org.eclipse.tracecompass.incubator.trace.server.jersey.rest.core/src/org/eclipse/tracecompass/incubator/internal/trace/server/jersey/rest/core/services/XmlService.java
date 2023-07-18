/*******************************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services;

import java.io.File;
import java.util.EnumSet;

import javax.ws.rs.core.Response;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module.XmlAnalysisModuleSource;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module.XmlOutputElement;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module.XmlUtils;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module.XmlUtils.OutputType;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.output.XmlDataProviderManager;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlUtils;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataModel;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataProvider;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.w3c.dom.Element;

import com.google.common.collect.Iterables;

/**
 * XML analysis and provider management
 *
 * @author Loic Prieur-Drevon
 * @author Ahmad Faour
 */
@SuppressWarnings("restriction")
public class XmlService {
    private static XmlService xmlService;

    private final XmlDataProviderManager xmlDataProviderManager;


    private XmlService() {
        this.xmlDataProviderManager = XmlDataProviderManager.getInstance();
    }

    /**
     * @return an instance of XmlService
     */
    public static XmlService getInstance() {
        if (xmlService == null) {
            xmlService = new XmlService();
        }

        return xmlService;
    }

    /**
     * Get the XML data provider for a trace, provider id and XML
     * {@link OutputType}
     *
     * @param trace
     *            the queried trace
     * @param id
     *            the queried ID
     * @param types
     *            the data provider type
     * @return the provider if an XML containing the ID exists and applies to
     *         the trace, else null
     */
    @SuppressWarnings("unchecked")
    public <@Nullable P extends ITmfTreeDataProvider<? extends @NonNull ITmfTreeDataModel>> P getXmlProvider(@NonNull ITmfTrace trace, @NonNull String id, EnumSet<OutputType> types) {
        for (OutputType viewType : types) {
            for (XmlOutputElement element : Iterables.filter(XmlUtils.getXmlOutputElements().values(),
                    element -> element.getXmlElem().equals(viewType.getXmlElem()) && id.equals(element.getId()))) {
                Element viewElement = TmfXmlUtils.getElementInFile(element.getPath(), viewType.getXmlElem(), id);
                if (viewElement != null && viewType == OutputType.XY) {
                    return (P) this.xmlDataProviderManager.getXyProvider(trace, viewElement);
                } else if (viewElement != null && viewType == OutputType.TIME_GRAPH) {
                    return (P) this.xmlDataProviderManager.getTimeGraphProvider(trace, viewElement);
                }
            }
        }
        return null;
    }

    /**
     * @param path of the file
     * @param addFile if true
     * @return Response
     */
    public Response updateXml(String path, boolean addFile) {
        File file = new File(path);

        IStatus status = XmlUtils.xmlValidate(file);
        if (status.isOK()) {
            if (addFile) {
                status = XmlUtils.addXmlFile(file);
            } else {
                XmlUtils.updateXmlFile(file);
            }
            if (status.isOK()) {
                XmlAnalysisModuleSource.notifyModuleChange();
                XmlUtils.saveFilesStatus();
                return Response.ok().build();
            }
        }
        return Response.serverError().build();
    }


}
