package org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services;

import static org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services.EndpointConstants.NO_PROVIDER;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.Response.Status;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.common.core.log.TraceCompassLog;
import org.eclipse.tracecompass.common.core.log.TraceCompassLogUtils.FlowScopeLog;
import org.eclipse.tracecompass.common.core.log.TraceCompassLogUtils.FlowScopeLogBuilder;
import org.eclipse.tracecompass.tmf.core.dataprovider.DataProviderManager;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataModel;
import org.eclipse.tracecompass.tmf.core.model.xy.ITmfTreeXYDataProvider;
import org.eclipse.tracecompass.tmf.core.model.xy.ITmfXyModel;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;

import com.fasterxml.jackson.databind.ObjectMapper;

@SuppressWarnings({ "null", "javadoc", "unchecked" })
public class XyService {

    private static XyService xyService;

    private final DataProviderManager dataProviderManager;
    private final @NonNull Logger logger;

    private XyService() {
        this.logger = TraceCompassLog.getLogger(TreeService.class);
        this.dataProviderManager = DataProviderManager.getInstance();

    }

    /**
     * @return an instance of ExperimentService
     */
    public static XyService getInstance() {
        if (xyService == null) {
            xyService = new XyService();
        }

        return xyService;
    }

    public TmfModelResponse<@NonNull ITmfXyModel> getXy(TmfExperiment tmfExperiment, String outputId, GetXyRequestDto parameters) {
        try (FlowScopeLog scope = new FlowScopeLogBuilder(this.logger, Level.FINE, "XyService#getXy") //$NON-NLS-1$
                .setCategory(outputId).build()) {

            ITmfTreeXYDataProvider<@NonNull ITmfTreeDataModel> provider = this.dataProviderManager.getOrCreateDataProvider(tmfExperiment,
                    outputId, ITmfTreeXYDataProvider.class);

            if (provider == null) {
                // The analysis cannot be run on this trace
                throw new ClientErrorException(NO_PROVIDER, Status.METHOD_NOT_ALLOWED);
            }

            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> mapParameters = objectMapper.convertValue(parameters, Map.class);
            String errorMessage = QueryParametersUtil.validateRequestedQueryParameters(mapParameters);
            if (errorMessage != null) {
                throw new BadRequestException(errorMessage);
            }

            errorMessage = QueryParametersUtil.validateRequestedQueryParameters(mapParameters);
            if (errorMessage != null) {
                throw new BadRequestException(errorMessage);
            }

            return provider.fetchXY(mapParameters, null);
        }
    }
}
