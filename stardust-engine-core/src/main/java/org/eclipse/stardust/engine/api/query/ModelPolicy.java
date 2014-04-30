package org.eclipse.stardust.engine.api.query;

/**
 * The ModelPolicy can be used to enforce DeployedModelQueries to exclude the
 * deployed predefined model
 *
 * @author thomas.wolfram
 *
 */
public class ModelPolicy implements EvaluationPolicy
{

	private static final long serialVersionUID = 1L;

	private boolean excludePredefinedModels;

	private ModelPolicy(boolean excludePredefined)
	{
		this.excludePredefinedModels = excludePredefined;
	}

	/**
	 *
	 * @return <code>true</code> if the {@link DeployedModelQuery} excludes the PredefinedModel,
	 * 		   <code>false</code> if no restrictions are applied
	 */
	public boolean isExcludePredefinedModels()
	{
		return excludePredefinedModels;
	}

	/**
	 * Initialize a ModelPolicy that excludes the PredefinedModel from the {@link DeployedModelQuery}
	 *
	 * @return {@link ModelPolicy}
	 */
	public static ModelPolicy excludePredefinedModels()
	{
		return new ModelPolicy(true);
	}

}
