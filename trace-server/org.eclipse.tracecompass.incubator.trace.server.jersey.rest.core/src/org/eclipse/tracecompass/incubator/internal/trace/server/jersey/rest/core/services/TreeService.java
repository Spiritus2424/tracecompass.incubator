package org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services;

import static org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services.EndpointConstants.NO_PROVIDER;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.validation.constraints.NotNull;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.Response.Status;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.common.core.log.TraceCompassLog;
import org.eclipse.tracecompass.common.core.log.TraceCompassLogUtils.FlowScopeLog;
import org.eclipse.tracecompass.common.core.log.TraceCompassLogUtils.FlowScopeLogBuilder;
import org.eclipse.tracecompass.tmf.core.dataprovider.DataProviderManager;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataModel;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataProvider;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;

public class TreeService {

    private static TreeService treeService;

    private final DataProviderManager dataProviderManager;
    private final @NonNull Logger logger;

    private TreeService() {
        this.logger = TraceCompassLog.getLogger(TreeService.class);
        this.dataProviderManager = DataProviderManager.getInstance();
    }

    /**
     * @return an instance of TreeService
     */
    public static TreeService getInstance() {
        if (treeService == null) {
            treeService = new TreeService();
        }

        return treeService;
    }


    /**
     * @param tmfExperiment experiment
     * @param outputId the provider id
     * @param parameters the get tree request dto
     * @return TmfModelResponse
     */
    @SuppressWarnings("unchecked")
    public TmfModelResponse<?> getTree(@NotNull TmfExperiment tmfExperiment, @NotNull String outputId, @NotNull GetTreeRequestDto parameters) {
        try (FlowScopeLog scope = new FlowScopeLogBuilder(this.logger, Level.FINE, "TreeService#getTree").setCategory(outputId).build()) { //$NON-NLS-1$

            ITmfTreeDataProvider<? extends @NonNull ITmfTreeDataModel> provider = this.dataProviderManager.getOrCreateDataProvider(
                    tmfExperiment,
                    outputId,
                    ITmfTreeDataProvider.class);

            if (provider == null) {
                // The analysis cannot be run on this trace
                throw new ClientErrorException(NO_PROVIDER, Status.METHOD_NOT_ALLOWED);
            }
            if (parameters.requestedTimes == null || parameters.requestedTimes.isEmpty()) {
                parameters.requestedTimes = ImmutableList.of(tmfExperiment.getStartTime().toNanos(), tmfExperiment.getEndTime().toNanos());
            }
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> mapParameters = objectMapper.convertValue(parameters, Map.class);
            String errorMessage = QueryParametersUtil.validateTreeQueryParameters(mapParameters);
            if (errorMessage != null) {
                throw new BadRequestException(errorMessage);
            }
            return provider.fetchTree(mapParameters, null);
        }
    }

}
