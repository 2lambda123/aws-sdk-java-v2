/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package software.amazon.awssdk.codegen.emitters.tasks;

import software.amazon.awssdk.codegen.emitters.GeneratorTaskParams;

/**
 * Generator tasks for AWS style clients.
 */
public class AwsGeneratorTasks extends CompositeGeneratorTask {
    public AwsGeneratorTasks(GeneratorTaskParams params) {
        super(new CommonGeneratorTasks(params),
              new AsyncClientGeneratorTasks(params),
              new PaginatorsGeneratorTasks(params),
              new EventStreamGeneratorTasks(params),
              new WaitersGeneratorTasks(params),
              new EndpointProviderTasks(params),
              new AuthSchemeGeneratorTasks(params));
    }
}
