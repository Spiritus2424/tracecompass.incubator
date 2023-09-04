package org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services;

import static org.eclipse.tracecompass.incubator.internal.trace.server.jersey.rest.core.services.EndpointConstants.NO_PROVIDER;

import java.util.List;
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
import org.eclipse.tracecompass.tmf.core.action.ITmfActionDescriptor;
import org.eclipse.tracecompass.tmf.core.dataprovider.DataProviderManager;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphArrow;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphDataProvider;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphEntryModel;
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphModel;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;

import com.fasterxml.jackson.databind.ObjectMapper;

@SuppressWarnings({ "null", "javadoc", "unchecked" })
public class TimeGraphService {

    private static TimeGraphService timeGraphService;

    private final DataProviderManager dataProviderManager;
    private final @NonNull Logger logger;

    private TimeGraphService() {
        this.logger = TraceCompassLog.getLogger(TreeService.class);
        this.dataProviderManager = DataProviderManager.getInstance();

    }

    /**
     * @return an instance of ExperimentService
     */
    public static TimeGraphService getInstance() {
        if (timeGraphService == null) {
            timeGraphService = new TimeGraphService();
        }

        return timeGraphService;
    }



    public TmfModelResponse<@NonNull TimeGraphModel> getStates(TmfExperiment tmfExperiment, String outputId, GetTimeGraphStatesRequestDto parameters) {
        try (FlowScopeLog scope = new FlowScopeLogBuilder(this.logger, Level.FINE, "TimeGraphService#getStates") //$NON-NLS-1$
                .setCategory(outputId).build()) {

            ITimeGraphDataProvider<@NonNull ITimeGraphEntryModel> provider = this.dataProviderManager.getOrCreateDataProvider(tmfExperiment,
                    outputId, ITimeGraphDataProvider.class);

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

            errorMessage = QueryParametersUtil.validateFilterQueryParameters(mapParameters);
            if (errorMessage != null) {
                throw new BadRequestException(errorMessage);
            }
            return provider.fetchRowModel(mapParameters, null);
        }
    }


    public TmfModelResponse<@NonNull List<@NonNull ITimeGraphArrow>> getArrows(TmfExperiment tmfExperiment, String outputId, GetTimeGraphArrowsRequestDto parameters) {
        try (FlowScopeLog scope = new FlowScopeLogBuilder(this.logger, Level.FINE, "TimeGraphService#getArrows").setCategory(outputId).build()) {//$NON-NLS-1$

            ITimeGraphDataProvider<@NonNull ITimeGraphEntryModel> provider = this.dataProviderManager.getOrCreateDataProvider(tmfExperiment,
                    outputId, ITimeGraphDataProvider.class);

            if (provider == null) {
                // The analysis cannot be run on this trace
                throw new ClientErrorException(NO_PROVIDER, Status.METHOD_NOT_ALLOWED);
            }

            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> mapParameters = objectMapper.convertValue(parameters, Map.class);
            String errorMessage = QueryParametersUtil.validateArrowsQueryParameters(mapParameters);
            if (errorMessage != null) {
                throw new BadRequestException(errorMessage);
            }
            return provider.fetchArrows(mapParameters, null);
        }
    }

    public TmfModelResponse<@NonNull Map<@NonNull String, @NonNull String>> getTooltips(TmfExperiment tmfExperiment, String outputId, GetTimeGraphTooltipsDto parameters) {
        try (FlowScopeLog scope = new FlowScopeLogBuilder(this.logger, Level.FINE, "TimeGraphService#getTooltips").setCategory(outputId).build()) { //$NON-NLS-1$

            ITimeGraphDataProvider<@NonNull ITimeGraphEntryModel> provider =  this.dataProviderManager.getOrCreateDataProvider(tmfExperiment,
                    outputId, ITimeGraphDataProvider.class);

            if (provider == null) {
                // The analysis cannot be run on this trace
                throw new ClientErrorException(NO_PROVIDER, Status.METHOD_NOT_ALLOWED);
            }

            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> mapParameters = objectMapper.convertValue(parameters, Map.class);
            String errorMessage = QueryParametersUtil.validateTooltipQueryParameters(mapParameters);
            if (errorMessage != null) {
                throw new BadRequestException(errorMessage);
            }
            return provider.fetchTooltip(mapParameters, null);
        }
    }

    public TmfModelResponse<@NonNull List<@NonNull ITmfActionDescriptor>> getActionTooltips(TmfExperiment tmfExperiment, String outputId, GetTimeGraphTooltipsDto parameters) {
        try (FlowScopeLog scope = new FlowScopeLogBuilder(this.logger, Level.FINE, "TimeGraphService#getActionTooltips").setCategory(outputId).build()) { //$NON-NLS-1$


            ITimeGraphDataProvider<@NonNull ITimeGraphEntryModel> provider = this.dataProviderManager.getOrCreateDataProvider(tmfExperiment,
                        outputId, ITimeGraphDataProvider.class);

            if (provider == null) {
                // The analysis cannot be run on this trace
                throw new ClientErrorException(NO_PROVIDER, Status.METHOD_NOT_ALLOWED);
            }

            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> mapParameters = objectMapper.convertValue(parameters, Map.class);
            String errorMessage = QueryParametersUtil.validateTooltipQueryParameters(mapParameters);
            if (errorMessage != null) {
                throw new BadRequestException(errorMessage);
            }

            return provider.fetchActionTooltips(mapParameters, null);
        }
    }

    public void applyAction(TmfExperiment tmfExperiment, String outputId, String actionId, Map<String, Object> inputParameters) {
        try (FlowScopeLog scope = new FlowScopeLogBuilder(this.logger, Level.FINE, "TimeGraphService#getTooltips").setCategory(outputId).build()) { //$NON-NLS-1$

            ITimeGraphDataProvider<@NonNull ITimeGraphEntryModel> provider =  this.dataProviderManager.getOrCreateDataProvider(tmfExperiment,
                    outputId, ITimeGraphDataProvider.class);

            if (provider == null) {
                // The analysis cannot be run on this trace
                throw new ClientErrorException(NO_PROVIDER, Status.METHOD_NOT_ALLOWED);
            }


            // TODO: Remove when this issue https://github.com/eclipse-cdt-cloud/theia-trace-extension/issues/1003 is fixed
            TmfSignalManager.dispatchSignal(new TmfTraceSelectedSignal(this, tmfExperiment));
            if (!provider.applyAction(actionId, inputParameters, null)) {
                throw new BadRequestException("Not able to applied the action: " + actionId); //$NON-NLS-1$
            }

        }
    }

}
