/* 
 * Copyright 2004 Apache Software Foundation
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.avalon.composition.model.impl;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.ArrayList;

import org.apache.avalon.composition.data.DeploymentProfile;
import org.apache.avalon.composition.info.DeliveryDescriptor;
import org.apache.avalon.composition.info.StagedDeliveryDescriptor;
import org.apache.avalon.composition.model.ContainmentModel;
import org.apache.avalon.composition.model.ComponentModel;
import org.apache.avalon.composition.model.DeploymentModel;
import org.apache.avalon.composition.model.AssemblyException;
import org.apache.avalon.composition.model.ProviderNotFoundException;
import org.apache.avalon.composition.model.ModelRepository;
import org.apache.avalon.composition.model.ContextModel;
import org.apache.avalon.composition.model.DependencyModel;
import org.apache.avalon.composition.model.StageModel;
import org.apache.avalon.composition.model.TypeUnknownException;
import org.apache.avalon.composition.model.ModelRuntimeException;
import org.apache.avalon.composition.model.ModelException;
import org.apache.avalon.composition.model.TypeRepository;
import org.apache.avalon.composition.model.ModelSelector;
import org.apache.avalon.composition.model.ProfileSelector;
import org.apache.avalon.composition.provider.ContainmentContext;

import org.apache.avalon.meta.info.Type;
import org.apache.avalon.meta.info.DependencyDescriptor;
import org.apache.avalon.meta.info.ReferenceDescriptor;
import org.apache.avalon.meta.info.StageDescriptor;

import org.apache.avalon.util.i18n.ResourceManager;
import org.apache.avalon.util.i18n.Resources;

/**
 * A utility class that assists in the location of a model relative
 * a supplied path.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: DefaultContainmentModelAssemblyHelper.java 46037 2004-09-14 21:04:40Z niclas $
 */
class DefaultContainmentModelAssemblyHelper
{
    //-------------------------------------------------------------------
    // static
    //-------------------------------------------------------------------

    private static final Resources REZ = ResourceManager
            .getPackageResources( DefaultContainmentModelAssemblyHelper.class );

    //-------------------------------------------------------------------
    // immutable state
    //-------------------------------------------------------------------

    private final ContainmentContext m_context;

    private final DefaultContainmentModel m_model;

    //-------------------------------------------------------------------
    // constructor
    //-------------------------------------------------------------------

    public DefaultContainmentModelAssemblyHelper( ContainmentContext context,
            DefaultContainmentModel model )
    {
        m_context = context;
        m_model = model;
    }

    private void assembleComponent( ComponentModel model, List subjects )
            throws AssemblyException
    {
        ModelRepository repository = m_context.getModelRepository();

        //
        // locate and assemble the component context handler
        //

        ContextModel context = model.getContextModel();
        DeliveryDescriptor delivery = context.getDeliveryDescriptor();
        if( context.isEnabled() )
        {
            if( delivery instanceof StagedDeliveryDescriptor )
            {
                if( null == context.getProvider() )
                {
                    StagedDeliveryDescriptor phased = (StagedDeliveryDescriptor) delivery;
                    Class clazz = phased.getDeliveryInterfaceClass();

                    try
                    {
                        subjects.add( model );
                        StageDescriptor stage = new StageDescriptor( clazz
                                .getName() );
                        DeploymentModel provider = findExtensionProvider(
                                repository, stage, subjects );
                        context.setProvider( provider );
                    }
                    catch ( Throwable e )
                    {
                        final String error = "Unable to assemble component: "
                                + model
                                + " due to a component context phase handler establishment failure.";
                        throw new AssemblyException( error, e );
                    }
                    finally
                    {
                        subjects.remove( model );
                    }
                }
            }
        }

        //
        // locate and resolve the stage providers
        //

        StageModel[] stages = model.getStageModels();
        for ( int i = 0; i < stages.length; i++ )
        {
            StageModel stage = stages[i];
            if( null == stage.getProvider() )
            {
                try
                {
                    subjects.add( model );
                    DeploymentModel provider = findExtensionProvider(
                            repository, stage, subjects );
                    stage.setProvider( provider );
                }
                catch ( Throwable e )
                {
                    final String error = "Unable to assemble component: "
                            + model
                            + " due to a component extension handler establishment failure.";
                    throw new AssemblyException( error, e );
                }
                finally
                {
                    subjects.remove( model );
                }
            }
        }

        //
        // locate and resolve the service providers
        //

        DependencyModel[] dependencies = model.getDependencyModels();
        for ( int i = 0; i < dependencies.length; i++ )
        {
            DependencyModel dependency = dependencies[i];
            if( null == dependency.getProvider() )
            {
                try
                {
                    subjects.add( model );
                    DeploymentModel provider = findDependencyProvider(
                            repository, dependency, subjects );
                    dependency.setProvider( provider );
                }
                catch ( Throwable e )
                {
                    if( dependency.getDependency().isRequired() )
                    {
                        final String error = "Unable to assemble component: "
                                + model
                                + " due to a service provider establishment failure.";
                        throw new AssemblyException( error, e );
                    }
                }
                finally
                {
                    subjects.remove( model );
                }
            }
        }
    }

    //-------------------------------------------------------------------
    // implementation
    //-------------------------------------------------------------------

    /**
     * Assemble a target model during which all deployment and runtime
     * dependencies are assigned a provider model.
     * 
     * @param model
     *            the target model to be assembled
     * @param subject
     *            the model requesting the assembly
     */
    public void assembleModel( DeploymentModel model, List subjects )
            throws AssemblyException
    {
        if( null == model )
        {
            throw new NullPointerException( "model" );
        }
        if( null == subjects )
        {
            throw new NullPointerException( "subjects" );
        }
        if( subjects.contains( model ) )
        {
            return;
        }
        if( model.isAssembled() )
        {
            return;
        }

        if( model instanceof ComponentModel )
        {
            assembleComponent( (ComponentModel) model, subjects );
        }
        else
        {
            ContainmentModel containment = (ContainmentModel) model;
            containment.assemble( subjects );
        }
    }

    /**
     * @param dependency
     *            the dependency to check for
     * @param subjects
     *            the subjects needing the dependency
     * @throws AssemblyException
     */
    private void checkCyclic( DeploymentModel dependency, List subjects )
            throws AssemblyException
    {
        if( subjects.contains( dependency ) )
        {
            throw new AssemblyException( "Cyclic Dependency: " + dependency
                    + "is already in subject list " + subjects );
        }
    }

    private DeploymentProfile[] findDependencyProfiles(
            DependencyDescriptor dependency )
    {
        TypeRepository repository = m_context.getClassLoaderModel()
                .getTypeRepository();
        Type[] types = repository.getTypes( dependency );
        try
        {
            return getProfiles( repository, types );
        }
        catch ( TypeUnknownException tue )
        {
            // will not happen
            final String error = "An irrational condition has occured.";
            throw new ModelRuntimeException( error, tue );
        }
    }

    DeploymentModel findDependencyProvider( DependencyDescriptor dependency )
            throws AssemblyException
    {
        ArrayList list = new ArrayList();
        ModelRepository repository = m_context.getModelRepository();
        return findDependencyProvider( repository, dependency, list );
    }

    private DeploymentModel findDependencyProvider( ModelRepository repository,
            DependencyDescriptor dependency, List subjects )
            throws AssemblyException
    {
        DeploymentModel[] candidates = repository
                .getCandidateProviders( dependency );
        ModelSelector selector = new DefaultModelSelector();
        DeploymentModel model = selector.select( candidates, dependency );
        //checkCyclic( model, subjects );
        if( model != null )
        {
            assembleModel( model, subjects );
            return model;
        }

        //
        // otherwise, check for any packaged profiles that
        // we could use to construct the model
        //

        DeploymentProfile[] profiles = findDependencyProfiles( dependency );
        ProfileSelector profileSelector = new DefaultProfileSelector();
        DeploymentProfile profile = profileSelector.select( profiles,
                dependency );
        if( profile != null )
        {
            try
            {
                DeploymentModel solution = m_model
                        .createDeploymentModel( profile );
                assembleModel( solution, subjects );
                m_model.addModel( solution );
                return solution;
            }
            catch ( AssemblyException ae )
            {
                final String error = "Nested assembly failure while attempting to construct model"
                        + " for the profile: "
                        + profile
                        + " for the dependency: [" + dependency + "].";
                throw new AssemblyException( error, ae );
            }
            catch ( ModelException me )
            {
                final String error = "Nested model failure while attempting to add model"
                        + " for the profile: "
                        + profile
                        + " for the dependency: [" + dependency + "].";
                throw new AssemblyException( error, me );
            }
        }
        else
        {
            final String error = "Unable to locate a service provider for the dependency: [ "
                    + dependency + "].";
            throw new AssemblyException( error );
        }
    }

    private DeploymentModel findDependencyProvider( ModelRepository repository,
            DependencyModel dependency, List subjects )
            throws AssemblyException
    {
        String path = dependency.getPath();
        if( null != path )
        {
            DeploymentModel model = m_model.getModel( path );
            if( null == model )
            {
                final String error = "Could not locate a model at the address: ["
                        + path + "] in " + this + ".";
                throw new AssemblyException( error );
            }
            assembleModel( model, subjects );
            return model;
        }
        else
        {
            return findDependencyProvider( repository, dependency
                    .getDependency(), subjects );
        }
    }

    private DeploymentProfile[] findExtensionProfiles( StageDescriptor stage )
    {
        TypeRepository repository = m_context.getClassLoaderModel()
                .getTypeRepository();
        Type[] types = repository.getTypes( stage );
        try
        {
            return getProfiles( repository, types );
        }
        catch ( TypeUnknownException tue )
        {
            // will not happen
            final String error = "An irrational condition has occured.";
            throw new ModelRuntimeException( error, tue );
        }
    }

    private DeploymentModel findExtensionProvider( ModelRepository repository,
            StageDescriptor stage, List subjects ) throws AssemblyException
    {
        DeploymentModel[] candidates = repository.getCandidateProviders( stage );
        ModelSelector selector = new DefaultModelSelector();
        DeploymentModel model = selector.select( candidates, stage );
        if( model != null )
        {
            assembleModel( model, subjects );
            return model;
        }

        //
        // otherwise, check for any packaged profiles that
        // we could use to construct the model
        //

        DeploymentProfile[] profiles = findExtensionProfiles( stage );
        ProfileSelector profileSelector = new DefaultProfileSelector();
        DeploymentProfile profile = profileSelector.select( profiles, stage );
        if( profile != null )
        {
            try
            {
                DeploymentModel solution = m_model
                        .createDeploymentModel( profile );
                assembleModel( solution, subjects );
                m_model.addModel( solution );
                return solution;
            }
            catch ( AssemblyException ae )
            {
                final String error = "Nested assembly failure while attempting to construct model"
                        + " for the extension profile: ["
                        + profile
                        + "] for the stage dependency: [" + stage + "].";
                throw new AssemblyException( error, ae );
            }
            catch ( ModelException me )
            {
                final String error = "Nested model failure while attempting to add model"
                        + " for the extension profile: "
                        + profile
                        + " for the stage dependency: [" + stage + "].";
                throw new AssemblyException( error, me );
            }
        }
        else
        {
            final String error = "Unable to locate a extension provider for the stage: [ "
                    + stage + "].";
            throw new ProviderNotFoundException( error );
        }
    }

    private DeploymentModel findExtensionProvider( ModelRepository repository,
            StageModel stage, List subjects ) throws AssemblyException
    {
        String path = stage.getPath();
        if( null != path )
        {
            DeploymentModel model = m_model.getModel( path );
            if( null == model )
            {
                final String error = "Could not locate a model at the address: ["
                        + path + "] in " + this + ".";
                throw new AssemblyException( error );
            }
            assembleModel( model, subjects );
            return model;
        }
        else
        {
            return findExtensionProvider( repository, stage.getStage(),
                    subjects );
        }
    }

    private DeploymentProfile[] findServiceProfiles(
            ReferenceDescriptor reference )
    {
        TypeRepository repository = m_context.getClassLoaderModel()
                .getTypeRepository();
        Type[] types = repository.getTypes( reference );
        try
        {
            return getProfiles( repository, types );
        }
        catch ( TypeUnknownException tue )
        {
            // will not happen
            final String error = "An irrational condition has occured.";
            throw new ModelRuntimeException( error, tue );
        }
    }

    private DeploymentModel findServiceProvider( ModelRepository repository,
            ReferenceDescriptor reference, List subjects )
            throws AssemblyException
    {
        DeploymentModel[] candidates = repository
                .getCandidateProviders( reference );
        ModelSelector selector = new DefaultModelSelector();
        DeploymentModel model = selector.select( candidates, reference );
        if( model != null )
        {
            assembleModel( model, subjects );
            return model;
        }

        //
        // otherwise, check for any packaged profiles that
        // we could use to construct the model
        //

        DeploymentProfile[] profiles = findServiceProfiles( reference );
        ProfileSelector profileSelector = new DefaultProfileSelector();
        DeploymentProfile profile = profileSelector
                .select( profiles, reference );
        if( profile != null )
        {
            try
            {
                DeploymentModel solution = m_model
                        .createDeploymentModel( profile );
                assembleModel( solution, subjects );
                m_model.addModel( solution );
                return solution;
            }
            catch ( AssemblyException ae )
            {
                final String error = "Nested assembly failure while attempting to construct model"
                        + " for the profile: ["
                        + profile
                        + "] for the reference: [" + reference + "].";
                throw new AssemblyException( error, ae );
            }
            catch ( ModelException me )
            {
                final String error = "Nested model failure while attempting to add model"
                        + " for the profile: "
                        + profile
                        + " for the reference: [" + reference + "].";
                throw new AssemblyException( error, me );
            }
        }
        else
        {
            final String error = "Unable to locate a service provider for the reference: [ "
                    + reference + "].";
            throw new ProviderNotFoundException( error );
        }
    }

    DeploymentModel findServiceProvider( ReferenceDescriptor reference )
            throws AssemblyException
    {
        ArrayList list = new ArrayList();
        ModelRepository repository = m_context.getModelRepository();
        return findServiceProvider( repository, reference, list );
    }

    private DeploymentProfile[] getProfiles( TypeRepository repository,
            Type[] types ) throws TypeUnknownException
    {
        ArrayList list = new ArrayList();
        for ( int i = 0; i < types.length; i++ )
        {
            DeploymentProfile[] profiles = repository.getProfiles( types[i] );
            for ( int j = 0; j < profiles.length; j++ )
            {
                list.add( profiles[j] );
            }
        }
        return (DeploymentProfile[]) list.toArray( new DeploymentProfile[0] );
    }
}
