/*
 * Copyright 2021 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.aws.sdk.v2.service;

import io.micronaut.core.annotation.Internal;
import software.amazon.awssdk.services.accessanalyzer.AccessAnalyzerAsyncClient;
import software.amazon.awssdk.services.accessanalyzer.AccessAnalyzerAsyncClientBuilder;
import software.amazon.awssdk.services.accessanalyzer.AccessAnalyzerClient;
import software.amazon.awssdk.services.accessanalyzer.AccessAnalyzerClientBuilder;
import software.amazon.awssdk.services.acm.AcmAsyncClient;
import software.amazon.awssdk.services.acm.AcmAsyncClientBuilder;
import software.amazon.awssdk.services.acm.AcmClient;
import software.amazon.awssdk.services.acm.AcmClientBuilder;
import software.amazon.awssdk.services.acmpca.AcmPcaAsyncClient;
import software.amazon.awssdk.services.acmpca.AcmPcaAsyncClientBuilder;
import software.amazon.awssdk.services.acmpca.AcmPcaClient;
import software.amazon.awssdk.services.acmpca.AcmPcaClientBuilder;
import software.amazon.awssdk.services.alexaforbusiness.AlexaForBusinessAsyncClient;
import software.amazon.awssdk.services.alexaforbusiness.AlexaForBusinessAsyncClientBuilder;
import software.amazon.awssdk.services.alexaforbusiness.AlexaForBusinessClient;
import software.amazon.awssdk.services.alexaforbusiness.AlexaForBusinessClientBuilder;
import software.amazon.awssdk.services.amp.AmpAsyncClient;
import software.amazon.awssdk.services.amp.AmpAsyncClientBuilder;
import software.amazon.awssdk.services.amp.AmpClient;
import software.amazon.awssdk.services.amp.AmpClientBuilder;
import software.amazon.awssdk.services.amplify.AmplifyAsyncClient;
import software.amazon.awssdk.services.amplify.AmplifyAsyncClientBuilder;
import software.amazon.awssdk.services.amplify.AmplifyClient;
import software.amazon.awssdk.services.amplify.AmplifyClientBuilder;
import software.amazon.awssdk.services.amplifybackend.AmplifyBackendAsyncClient;
import software.amazon.awssdk.services.amplifybackend.AmplifyBackendAsyncClientBuilder;
import software.amazon.awssdk.services.amplifybackend.AmplifyBackendClient;
import software.amazon.awssdk.services.amplifybackend.AmplifyBackendClientBuilder;
import software.amazon.awssdk.services.apigateway.ApiGatewayAsyncClient;
import software.amazon.awssdk.services.apigateway.ApiGatewayAsyncClientBuilder;
import software.amazon.awssdk.services.apigateway.ApiGatewayClient;
import software.amazon.awssdk.services.apigateway.ApiGatewayClientBuilder;
import software.amazon.awssdk.services.apigatewaymanagementapi.ApiGatewayManagementApiAsyncClient;
import software.amazon.awssdk.services.apigatewaymanagementapi.ApiGatewayManagementApiAsyncClientBuilder;
import software.amazon.awssdk.services.apigatewaymanagementapi.ApiGatewayManagementApiClient;
import software.amazon.awssdk.services.apigatewaymanagementapi.ApiGatewayManagementApiClientBuilder;
import software.amazon.awssdk.services.apigatewayv2.ApiGatewayV2AsyncClient;
import software.amazon.awssdk.services.apigatewayv2.ApiGatewayV2AsyncClientBuilder;
import software.amazon.awssdk.services.apigatewayv2.ApiGatewayV2Client;
import software.amazon.awssdk.services.apigatewayv2.ApiGatewayV2ClientBuilder;
import software.amazon.awssdk.services.appconfig.AppConfigAsyncClient;
import software.amazon.awssdk.services.appconfig.AppConfigAsyncClientBuilder;
import software.amazon.awssdk.services.appconfig.AppConfigClient;
import software.amazon.awssdk.services.appconfig.AppConfigClientBuilder;
import software.amazon.awssdk.services.appflow.AppflowAsyncClient;
import software.amazon.awssdk.services.appflow.AppflowAsyncClientBuilder;
import software.amazon.awssdk.services.appflow.AppflowClient;
import software.amazon.awssdk.services.appflow.AppflowClientBuilder;
import software.amazon.awssdk.services.appintegrations.AppIntegrationsAsyncClient;
import software.amazon.awssdk.services.appintegrations.AppIntegrationsAsyncClientBuilder;
import software.amazon.awssdk.services.appintegrations.AppIntegrationsClient;
import software.amazon.awssdk.services.appintegrations.AppIntegrationsClientBuilder;
import software.amazon.awssdk.services.applicationautoscaling.ApplicationAutoScalingAsyncClient;
import software.amazon.awssdk.services.applicationautoscaling.ApplicationAutoScalingAsyncClientBuilder;
import software.amazon.awssdk.services.applicationautoscaling.ApplicationAutoScalingClient;
import software.amazon.awssdk.services.applicationautoscaling.ApplicationAutoScalingClientBuilder;
import software.amazon.awssdk.services.applicationdiscovery.ApplicationDiscoveryAsyncClient;
import software.amazon.awssdk.services.applicationdiscovery.ApplicationDiscoveryAsyncClientBuilder;
import software.amazon.awssdk.services.applicationdiscovery.ApplicationDiscoveryClient;
import software.amazon.awssdk.services.applicationdiscovery.ApplicationDiscoveryClientBuilder;
import software.amazon.awssdk.services.applicationinsights.ApplicationInsightsAsyncClient;
import software.amazon.awssdk.services.applicationinsights.ApplicationInsightsAsyncClientBuilder;
import software.amazon.awssdk.services.applicationinsights.ApplicationInsightsClient;
import software.amazon.awssdk.services.applicationinsights.ApplicationInsightsClientBuilder;
import software.amazon.awssdk.services.appmesh.AppMeshAsyncClient;
import software.amazon.awssdk.services.appmesh.AppMeshAsyncClientBuilder;
import software.amazon.awssdk.services.appmesh.AppMeshClient;
import software.amazon.awssdk.services.appmesh.AppMeshClientBuilder;
import software.amazon.awssdk.services.appstream.AppStreamAsyncClient;
import software.amazon.awssdk.services.appstream.AppStreamAsyncClientBuilder;
import software.amazon.awssdk.services.appstream.AppStreamClient;
import software.amazon.awssdk.services.appstream.AppStreamClientBuilder;
import software.amazon.awssdk.services.appsync.AppSyncAsyncClient;
import software.amazon.awssdk.services.appsync.AppSyncAsyncClientBuilder;
import software.amazon.awssdk.services.appsync.AppSyncClient;
import software.amazon.awssdk.services.appsync.AppSyncClientBuilder;
import software.amazon.awssdk.services.athena.AthenaAsyncClient;
import software.amazon.awssdk.services.athena.AthenaAsyncClientBuilder;
import software.amazon.awssdk.services.athena.AthenaClient;
import software.amazon.awssdk.services.athena.AthenaClientBuilder;
import software.amazon.awssdk.services.auditmanager.AuditManagerAsyncClient;
import software.amazon.awssdk.services.auditmanager.AuditManagerAsyncClientBuilder;
import software.amazon.awssdk.services.auditmanager.AuditManagerClient;
import software.amazon.awssdk.services.auditmanager.AuditManagerClientBuilder;
import software.amazon.awssdk.services.autoscaling.AutoScalingAsyncClient;
import software.amazon.awssdk.services.autoscaling.AutoScalingAsyncClientBuilder;
import software.amazon.awssdk.services.autoscaling.AutoScalingClient;
import software.amazon.awssdk.services.autoscaling.AutoScalingClientBuilder;
import software.amazon.awssdk.services.autoscalingplans.AutoScalingPlansAsyncClient;
import software.amazon.awssdk.services.autoscalingplans.AutoScalingPlansAsyncClientBuilder;
import software.amazon.awssdk.services.autoscalingplans.AutoScalingPlansClient;
import software.amazon.awssdk.services.autoscalingplans.AutoScalingPlansClientBuilder;
import software.amazon.awssdk.services.backup.BackupAsyncClient;
import software.amazon.awssdk.services.backup.BackupAsyncClientBuilder;
import software.amazon.awssdk.services.backup.BackupClient;
import software.amazon.awssdk.services.backup.BackupClientBuilder;
import software.amazon.awssdk.services.batch.BatchAsyncClient;
import software.amazon.awssdk.services.batch.BatchAsyncClientBuilder;
import software.amazon.awssdk.services.batch.BatchClient;
import software.amazon.awssdk.services.batch.BatchClientBuilder;
import software.amazon.awssdk.services.braket.BraketAsyncClient;
import software.amazon.awssdk.services.braket.BraketAsyncClientBuilder;
import software.amazon.awssdk.services.braket.BraketClient;
import software.amazon.awssdk.services.braket.BraketClientBuilder;
import software.amazon.awssdk.services.budgets.BudgetsAsyncClient;
import software.amazon.awssdk.services.budgets.BudgetsAsyncClientBuilder;
import software.amazon.awssdk.services.budgets.BudgetsClient;
import software.amazon.awssdk.services.budgets.BudgetsClientBuilder;
import software.amazon.awssdk.services.chime.ChimeAsyncClient;
import software.amazon.awssdk.services.chime.ChimeAsyncClientBuilder;
import software.amazon.awssdk.services.chime.ChimeClient;
import software.amazon.awssdk.services.chime.ChimeClientBuilder;
import software.amazon.awssdk.services.cloud9.Cloud9AsyncClient;
import software.amazon.awssdk.services.cloud9.Cloud9AsyncClientBuilder;
import software.amazon.awssdk.services.cloud9.Cloud9Client;
import software.amazon.awssdk.services.cloud9.Cloud9ClientBuilder;
import software.amazon.awssdk.services.clouddirectory.CloudDirectoryAsyncClient;
import software.amazon.awssdk.services.clouddirectory.CloudDirectoryAsyncClientBuilder;
import software.amazon.awssdk.services.clouddirectory.CloudDirectoryClient;
import software.amazon.awssdk.services.clouddirectory.CloudDirectoryClientBuilder;
import software.amazon.awssdk.services.cloudformation.CloudFormationAsyncClient;
import software.amazon.awssdk.services.cloudformation.CloudFormationAsyncClientBuilder;
import software.amazon.awssdk.services.cloudformation.CloudFormationClient;
import software.amazon.awssdk.services.cloudformation.CloudFormationClientBuilder;
import software.amazon.awssdk.services.cloudfront.CloudFrontAsyncClient;
import software.amazon.awssdk.services.cloudfront.CloudFrontAsyncClientBuilder;
import software.amazon.awssdk.services.cloudfront.CloudFrontClient;
import software.amazon.awssdk.services.cloudfront.CloudFrontClientBuilder;
import software.amazon.awssdk.services.cloudhsm.CloudHsmAsyncClient;
import software.amazon.awssdk.services.cloudhsm.CloudHsmAsyncClientBuilder;
import software.amazon.awssdk.services.cloudhsm.CloudHsmClient;
import software.amazon.awssdk.services.cloudhsm.CloudHsmClientBuilder;
import software.amazon.awssdk.services.cloudhsmv2.CloudHsmV2AsyncClient;
import software.amazon.awssdk.services.cloudhsmv2.CloudHsmV2AsyncClientBuilder;
import software.amazon.awssdk.services.cloudhsmv2.CloudHsmV2Client;
import software.amazon.awssdk.services.cloudhsmv2.CloudHsmV2ClientBuilder;
import software.amazon.awssdk.services.cloudsearch.CloudSearchAsyncClient;
import software.amazon.awssdk.services.cloudsearch.CloudSearchAsyncClientBuilder;
import software.amazon.awssdk.services.cloudsearch.CloudSearchClient;
import software.amazon.awssdk.services.cloudsearch.CloudSearchClientBuilder;
import software.amazon.awssdk.services.cloudsearchdomain.CloudSearchDomainAsyncClient;
import software.amazon.awssdk.services.cloudsearchdomain.CloudSearchDomainAsyncClientBuilder;
import software.amazon.awssdk.services.cloudsearchdomain.CloudSearchDomainClient;
import software.amazon.awssdk.services.cloudsearchdomain.CloudSearchDomainClientBuilder;
import software.amazon.awssdk.services.cloudtrail.CloudTrailAsyncClient;
import software.amazon.awssdk.services.cloudtrail.CloudTrailAsyncClientBuilder;
import software.amazon.awssdk.services.cloudtrail.CloudTrailClient;
import software.amazon.awssdk.services.cloudtrail.CloudTrailClientBuilder;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClientBuilder;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClientBuilder;
import software.amazon.awssdk.services.cloudwatchevents.CloudWatchEventsAsyncClient;
import software.amazon.awssdk.services.cloudwatchevents.CloudWatchEventsAsyncClientBuilder;
import software.amazon.awssdk.services.cloudwatchevents.CloudWatchEventsClient;
import software.amazon.awssdk.services.cloudwatchevents.CloudWatchEventsClientBuilder;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsAsyncClient;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsAsyncClientBuilder;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClientBuilder;
import software.amazon.awssdk.services.codeartifact.CodeartifactAsyncClient;
import software.amazon.awssdk.services.codeartifact.CodeartifactAsyncClientBuilder;
import software.amazon.awssdk.services.codeartifact.CodeartifactClient;
import software.amazon.awssdk.services.codeartifact.CodeartifactClientBuilder;
import software.amazon.awssdk.services.codebuild.CodeBuildAsyncClient;
import software.amazon.awssdk.services.codebuild.CodeBuildAsyncClientBuilder;
import software.amazon.awssdk.services.codebuild.CodeBuildClient;
import software.amazon.awssdk.services.codebuild.CodeBuildClientBuilder;
import software.amazon.awssdk.services.codecommit.CodeCommitAsyncClient;
import software.amazon.awssdk.services.codecommit.CodeCommitAsyncClientBuilder;
import software.amazon.awssdk.services.codecommit.CodeCommitClient;
import software.amazon.awssdk.services.codecommit.CodeCommitClientBuilder;
import software.amazon.awssdk.services.codedeploy.CodeDeployAsyncClient;
import software.amazon.awssdk.services.codedeploy.CodeDeployAsyncClientBuilder;
import software.amazon.awssdk.services.codedeploy.CodeDeployClient;
import software.amazon.awssdk.services.codedeploy.CodeDeployClientBuilder;
import software.amazon.awssdk.services.codeguruprofiler.CodeGuruProfilerAsyncClient;
import software.amazon.awssdk.services.codeguruprofiler.CodeGuruProfilerAsyncClientBuilder;
import software.amazon.awssdk.services.codeguruprofiler.CodeGuruProfilerClient;
import software.amazon.awssdk.services.codeguruprofiler.CodeGuruProfilerClientBuilder;
import software.amazon.awssdk.services.codegurureviewer.CodeGuruReviewerAsyncClient;
import software.amazon.awssdk.services.codegurureviewer.CodeGuruReviewerAsyncClientBuilder;
import software.amazon.awssdk.services.codegurureviewer.CodeGuruReviewerClient;
import software.amazon.awssdk.services.codegurureviewer.CodeGuruReviewerClientBuilder;
import software.amazon.awssdk.services.codepipeline.CodePipelineAsyncClient;
import software.amazon.awssdk.services.codepipeline.CodePipelineAsyncClientBuilder;
import software.amazon.awssdk.services.codepipeline.CodePipelineClient;
import software.amazon.awssdk.services.codepipeline.CodePipelineClientBuilder;
import software.amazon.awssdk.services.codestar.CodeStarAsyncClient;
import software.amazon.awssdk.services.codestar.CodeStarAsyncClientBuilder;
import software.amazon.awssdk.services.codestar.CodeStarClient;
import software.amazon.awssdk.services.codestar.CodeStarClientBuilder;
import software.amazon.awssdk.services.codestarconnections.CodeStarConnectionsAsyncClient;
import software.amazon.awssdk.services.codestarconnections.CodeStarConnectionsAsyncClientBuilder;
import software.amazon.awssdk.services.codestarconnections.CodeStarConnectionsClient;
import software.amazon.awssdk.services.codestarconnections.CodeStarConnectionsClientBuilder;
import software.amazon.awssdk.services.codestarnotifications.CodestarNotificationsAsyncClient;
import software.amazon.awssdk.services.codestarnotifications.CodestarNotificationsAsyncClientBuilder;
import software.amazon.awssdk.services.codestarnotifications.CodestarNotificationsClient;
import software.amazon.awssdk.services.codestarnotifications.CodestarNotificationsClientBuilder;
import software.amazon.awssdk.services.cognitoidentity.CognitoIdentityAsyncClient;
import software.amazon.awssdk.services.cognitoidentity.CognitoIdentityAsyncClientBuilder;
import software.amazon.awssdk.services.cognitoidentity.CognitoIdentityClient;
import software.amazon.awssdk.services.cognitoidentity.CognitoIdentityClientBuilder;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderAsyncClient;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderAsyncClientBuilder;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClientBuilder;
import software.amazon.awssdk.services.cognitosync.CognitoSyncAsyncClient;
import software.amazon.awssdk.services.cognitosync.CognitoSyncAsyncClientBuilder;
import software.amazon.awssdk.services.cognitosync.CognitoSyncClient;
import software.amazon.awssdk.services.cognitosync.CognitoSyncClientBuilder;
import software.amazon.awssdk.services.comprehend.ComprehendAsyncClient;
import software.amazon.awssdk.services.comprehend.ComprehendAsyncClientBuilder;
import software.amazon.awssdk.services.comprehend.ComprehendClient;
import software.amazon.awssdk.services.comprehend.ComprehendClientBuilder;
import software.amazon.awssdk.services.comprehendmedical.ComprehendMedicalAsyncClient;
import software.amazon.awssdk.services.comprehendmedical.ComprehendMedicalAsyncClientBuilder;
import software.amazon.awssdk.services.comprehendmedical.ComprehendMedicalClient;
import software.amazon.awssdk.services.comprehendmedical.ComprehendMedicalClientBuilder;
import software.amazon.awssdk.services.computeoptimizer.ComputeOptimizerAsyncClient;
import software.amazon.awssdk.services.computeoptimizer.ComputeOptimizerAsyncClientBuilder;
import software.amazon.awssdk.services.computeoptimizer.ComputeOptimizerClient;
import software.amazon.awssdk.services.computeoptimizer.ComputeOptimizerClientBuilder;
import software.amazon.awssdk.services.config.ConfigAsyncClient;
import software.amazon.awssdk.services.config.ConfigAsyncClientBuilder;
import software.amazon.awssdk.services.config.ConfigClient;
import software.amazon.awssdk.services.config.ConfigClientBuilder;
import software.amazon.awssdk.services.connect.ConnectAsyncClient;
import software.amazon.awssdk.services.connect.ConnectAsyncClientBuilder;
import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.ConnectClientBuilder;
import software.amazon.awssdk.services.connectcontactlens.ConnectContactLensAsyncClient;
import software.amazon.awssdk.services.connectcontactlens.ConnectContactLensAsyncClientBuilder;
import software.amazon.awssdk.services.connectcontactlens.ConnectContactLensClient;
import software.amazon.awssdk.services.connectcontactlens.ConnectContactLensClientBuilder;
import software.amazon.awssdk.services.connectparticipant.ConnectParticipantAsyncClient;
import software.amazon.awssdk.services.connectparticipant.ConnectParticipantAsyncClientBuilder;
import software.amazon.awssdk.services.connectparticipant.ConnectParticipantClient;
import software.amazon.awssdk.services.connectparticipant.ConnectParticipantClientBuilder;
import software.amazon.awssdk.services.costandusagereport.CostAndUsageReportAsyncClient;
import software.amazon.awssdk.services.costandusagereport.CostAndUsageReportAsyncClientBuilder;
import software.amazon.awssdk.services.costandusagereport.CostAndUsageReportClient;
import software.amazon.awssdk.services.costandusagereport.CostAndUsageReportClientBuilder;
import software.amazon.awssdk.services.costexplorer.CostExplorerAsyncClient;
import software.amazon.awssdk.services.costexplorer.CostExplorerAsyncClientBuilder;
import software.amazon.awssdk.services.costexplorer.CostExplorerClient;
import software.amazon.awssdk.services.costexplorer.CostExplorerClientBuilder;
import software.amazon.awssdk.services.customerprofiles.CustomerProfilesAsyncClient;
import software.amazon.awssdk.services.customerprofiles.CustomerProfilesAsyncClientBuilder;
import software.amazon.awssdk.services.customerprofiles.CustomerProfilesClient;
import software.amazon.awssdk.services.customerprofiles.CustomerProfilesClientBuilder;
import software.amazon.awssdk.services.databasemigration.DatabaseMigrationAsyncClient;
import software.amazon.awssdk.services.databasemigration.DatabaseMigrationAsyncClientBuilder;
import software.amazon.awssdk.services.databasemigration.DatabaseMigrationClient;
import software.amazon.awssdk.services.databasemigration.DatabaseMigrationClientBuilder;
import software.amazon.awssdk.services.databrew.DataBrewAsyncClient;
import software.amazon.awssdk.services.databrew.DataBrewAsyncClientBuilder;
import software.amazon.awssdk.services.databrew.DataBrewClient;
import software.amazon.awssdk.services.databrew.DataBrewClientBuilder;
import software.amazon.awssdk.services.dataexchange.DataExchangeAsyncClient;
import software.amazon.awssdk.services.dataexchange.DataExchangeAsyncClientBuilder;
import software.amazon.awssdk.services.dataexchange.DataExchangeClient;
import software.amazon.awssdk.services.dataexchange.DataExchangeClientBuilder;
import software.amazon.awssdk.services.datapipeline.DataPipelineAsyncClient;
import software.amazon.awssdk.services.datapipeline.DataPipelineAsyncClientBuilder;
import software.amazon.awssdk.services.datapipeline.DataPipelineClient;
import software.amazon.awssdk.services.datapipeline.DataPipelineClientBuilder;
import software.amazon.awssdk.services.datasync.DataSyncAsyncClient;
import software.amazon.awssdk.services.datasync.DataSyncAsyncClientBuilder;
import software.amazon.awssdk.services.datasync.DataSyncClient;
import software.amazon.awssdk.services.datasync.DataSyncClientBuilder;
import software.amazon.awssdk.services.dax.DaxAsyncClient;
import software.amazon.awssdk.services.dax.DaxAsyncClientBuilder;
import software.amazon.awssdk.services.dax.DaxClient;
import software.amazon.awssdk.services.dax.DaxClientBuilder;
import software.amazon.awssdk.services.detective.DetectiveAsyncClient;
import software.amazon.awssdk.services.detective.DetectiveAsyncClientBuilder;
import software.amazon.awssdk.services.detective.DetectiveClient;
import software.amazon.awssdk.services.detective.DetectiveClientBuilder;
import software.amazon.awssdk.services.devicefarm.DeviceFarmAsyncClient;
import software.amazon.awssdk.services.devicefarm.DeviceFarmAsyncClientBuilder;
import software.amazon.awssdk.services.devicefarm.DeviceFarmClient;
import software.amazon.awssdk.services.devicefarm.DeviceFarmClientBuilder;
import software.amazon.awssdk.services.devopsguru.DevOpsGuruAsyncClient;
import software.amazon.awssdk.services.devopsguru.DevOpsGuruAsyncClientBuilder;
import software.amazon.awssdk.services.devopsguru.DevOpsGuruClient;
import software.amazon.awssdk.services.devopsguru.DevOpsGuruClientBuilder;
import software.amazon.awssdk.services.directconnect.DirectConnectAsyncClient;
import software.amazon.awssdk.services.directconnect.DirectConnectAsyncClientBuilder;
import software.amazon.awssdk.services.directconnect.DirectConnectClient;
import software.amazon.awssdk.services.directconnect.DirectConnectClientBuilder;
import software.amazon.awssdk.services.directory.DirectoryAsyncClient;
import software.amazon.awssdk.services.directory.DirectoryAsyncClientBuilder;
import software.amazon.awssdk.services.directory.DirectoryClient;
import software.amazon.awssdk.services.directory.DirectoryClientBuilder;
import software.amazon.awssdk.services.dlm.DlmAsyncClient;
import software.amazon.awssdk.services.dlm.DlmAsyncClientBuilder;
import software.amazon.awssdk.services.dlm.DlmClient;
import software.amazon.awssdk.services.dlm.DlmClientBuilder;
import software.amazon.awssdk.services.docdb.DocDbAsyncClient;
import software.amazon.awssdk.services.docdb.DocDbAsyncClientBuilder;
import software.amazon.awssdk.services.docdb.DocDbClient;
import software.amazon.awssdk.services.docdb.DocDbClientBuilder;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClientBuilder;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClientBuilder;
import software.amazon.awssdk.services.dynamodb.streams.DynamoDbStreamsAsyncClient;
import software.amazon.awssdk.services.dynamodb.streams.DynamoDbStreamsAsyncClientBuilder;
import software.amazon.awssdk.services.dynamodb.streams.DynamoDbStreamsClient;
import software.amazon.awssdk.services.dynamodb.streams.DynamoDbStreamsClientBuilder;
import software.amazon.awssdk.services.ebs.EbsAsyncClient;
import software.amazon.awssdk.services.ebs.EbsAsyncClientBuilder;
import software.amazon.awssdk.services.ebs.EbsClient;
import software.amazon.awssdk.services.ebs.EbsClientBuilder;
import software.amazon.awssdk.services.ec2.Ec2AsyncClient;
import software.amazon.awssdk.services.ec2.Ec2AsyncClientBuilder;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.Ec2ClientBuilder;
import software.amazon.awssdk.services.ec2instanceconnect.Ec2InstanceConnectAsyncClient;
import software.amazon.awssdk.services.ec2instanceconnect.Ec2InstanceConnectAsyncClientBuilder;
import software.amazon.awssdk.services.ec2instanceconnect.Ec2InstanceConnectClient;
import software.amazon.awssdk.services.ec2instanceconnect.Ec2InstanceConnectClientBuilder;
import software.amazon.awssdk.services.ecr.EcrAsyncClient;
import software.amazon.awssdk.services.ecr.EcrAsyncClientBuilder;
import software.amazon.awssdk.services.ecr.EcrClient;
import software.amazon.awssdk.services.ecr.EcrClientBuilder;
import software.amazon.awssdk.services.ecrpublic.EcrPublicAsyncClient;
import software.amazon.awssdk.services.ecrpublic.EcrPublicAsyncClientBuilder;
import software.amazon.awssdk.services.ecrpublic.EcrPublicClient;
import software.amazon.awssdk.services.ecrpublic.EcrPublicClientBuilder;
import software.amazon.awssdk.services.ecs.EcsAsyncClient;
import software.amazon.awssdk.services.ecs.EcsAsyncClientBuilder;
import software.amazon.awssdk.services.ecs.EcsClient;
import software.amazon.awssdk.services.ecs.EcsClientBuilder;
import software.amazon.awssdk.services.efs.EfsAsyncClient;
import software.amazon.awssdk.services.efs.EfsAsyncClientBuilder;
import software.amazon.awssdk.services.efs.EfsClient;
import software.amazon.awssdk.services.efs.EfsClientBuilder;
import software.amazon.awssdk.services.eks.EksAsyncClient;
import software.amazon.awssdk.services.eks.EksAsyncClientBuilder;
import software.amazon.awssdk.services.eks.EksClient;
import software.amazon.awssdk.services.eks.EksClientBuilder;
import software.amazon.awssdk.services.elasticache.ElastiCacheAsyncClient;
import software.amazon.awssdk.services.elasticache.ElastiCacheAsyncClientBuilder;
import software.amazon.awssdk.services.elasticache.ElastiCacheClient;
import software.amazon.awssdk.services.elasticache.ElastiCacheClientBuilder;
import software.amazon.awssdk.services.elasticbeanstalk.ElasticBeanstalkAsyncClient;
import software.amazon.awssdk.services.elasticbeanstalk.ElasticBeanstalkAsyncClientBuilder;
import software.amazon.awssdk.services.elasticbeanstalk.ElasticBeanstalkClient;
import software.amazon.awssdk.services.elasticbeanstalk.ElasticBeanstalkClientBuilder;
import software.amazon.awssdk.services.elasticinference.ElasticInferenceAsyncClient;
import software.amazon.awssdk.services.elasticinference.ElasticInferenceAsyncClientBuilder;
import software.amazon.awssdk.services.elasticinference.ElasticInferenceClient;
import software.amazon.awssdk.services.elasticinference.ElasticInferenceClientBuilder;
import software.amazon.awssdk.services.elasticloadbalancing.ElasticLoadBalancingAsyncClient;
import software.amazon.awssdk.services.elasticloadbalancing.ElasticLoadBalancingAsyncClientBuilder;
import software.amazon.awssdk.services.elasticloadbalancing.ElasticLoadBalancingClient;
import software.amazon.awssdk.services.elasticloadbalancing.ElasticLoadBalancingClientBuilder;
import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2AsyncClient;
import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2AsyncClientBuilder;
import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client;
import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2ClientBuilder;
import software.amazon.awssdk.services.elasticsearch.ElasticsearchAsyncClient;
import software.amazon.awssdk.services.elasticsearch.ElasticsearchAsyncClientBuilder;
import software.amazon.awssdk.services.elasticsearch.ElasticsearchClient;
import software.amazon.awssdk.services.elasticsearch.ElasticsearchClientBuilder;
import software.amazon.awssdk.services.elastictranscoder.ElasticTranscoderAsyncClient;
import software.amazon.awssdk.services.elastictranscoder.ElasticTranscoderAsyncClientBuilder;
import software.amazon.awssdk.services.elastictranscoder.ElasticTranscoderClient;
import software.amazon.awssdk.services.elastictranscoder.ElasticTranscoderClientBuilder;
import software.amazon.awssdk.services.emr.EmrAsyncClient;
import software.amazon.awssdk.services.emr.EmrAsyncClientBuilder;
import software.amazon.awssdk.services.emr.EmrClient;
import software.amazon.awssdk.services.emr.EmrClientBuilder;
import software.amazon.awssdk.services.emrcontainers.EmrContainersAsyncClient;
import software.amazon.awssdk.services.emrcontainers.EmrContainersAsyncClientBuilder;
import software.amazon.awssdk.services.emrcontainers.EmrContainersClient;
import software.amazon.awssdk.services.emrcontainers.EmrContainersClientBuilder;
import software.amazon.awssdk.services.eventbridge.EventBridgeAsyncClient;
import software.amazon.awssdk.services.eventbridge.EventBridgeAsyncClientBuilder;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.EventBridgeClientBuilder;
import software.amazon.awssdk.services.firehose.FirehoseAsyncClient;
import software.amazon.awssdk.services.firehose.FirehoseAsyncClientBuilder;
import software.amazon.awssdk.services.firehose.FirehoseClient;
import software.amazon.awssdk.services.firehose.FirehoseClientBuilder;
import software.amazon.awssdk.services.fis.FisAsyncClient;
import software.amazon.awssdk.services.fis.FisAsyncClientBuilder;
import software.amazon.awssdk.services.fis.FisClient;
import software.amazon.awssdk.services.fis.FisClientBuilder;
import software.amazon.awssdk.services.fms.FmsAsyncClient;
import software.amazon.awssdk.services.fms.FmsAsyncClientBuilder;
import software.amazon.awssdk.services.fms.FmsClient;
import software.amazon.awssdk.services.fms.FmsClientBuilder;
import software.amazon.awssdk.services.forecast.ForecastAsyncClient;
import software.amazon.awssdk.services.forecast.ForecastAsyncClientBuilder;
import software.amazon.awssdk.services.forecast.ForecastClient;
import software.amazon.awssdk.services.forecast.ForecastClientBuilder;
import software.amazon.awssdk.services.forecastquery.ForecastqueryAsyncClient;
import software.amazon.awssdk.services.forecastquery.ForecastqueryAsyncClientBuilder;
import software.amazon.awssdk.services.forecastquery.ForecastqueryClient;
import software.amazon.awssdk.services.forecastquery.ForecastqueryClientBuilder;
import software.amazon.awssdk.services.frauddetector.FraudDetectorAsyncClient;
import software.amazon.awssdk.services.frauddetector.FraudDetectorAsyncClientBuilder;
import software.amazon.awssdk.services.frauddetector.FraudDetectorClient;
import software.amazon.awssdk.services.frauddetector.FraudDetectorClientBuilder;
import software.amazon.awssdk.services.fsx.FSxAsyncClient;
import software.amazon.awssdk.services.fsx.FSxAsyncClientBuilder;
import software.amazon.awssdk.services.fsx.FSxClient;
import software.amazon.awssdk.services.fsx.FSxClientBuilder;
import software.amazon.awssdk.services.gamelift.GameLiftAsyncClient;
import software.amazon.awssdk.services.gamelift.GameLiftAsyncClientBuilder;
import software.amazon.awssdk.services.gamelift.GameLiftClient;
import software.amazon.awssdk.services.gamelift.GameLiftClientBuilder;
import software.amazon.awssdk.services.glacier.GlacierAsyncClient;
import software.amazon.awssdk.services.glacier.GlacierAsyncClientBuilder;
import software.amazon.awssdk.services.glacier.GlacierClient;
import software.amazon.awssdk.services.glacier.GlacierClientBuilder;
import software.amazon.awssdk.services.globalaccelerator.GlobalAcceleratorAsyncClient;
import software.amazon.awssdk.services.globalaccelerator.GlobalAcceleratorAsyncClientBuilder;
import software.amazon.awssdk.services.globalaccelerator.GlobalAcceleratorClient;
import software.amazon.awssdk.services.globalaccelerator.GlobalAcceleratorClientBuilder;
import software.amazon.awssdk.services.glue.GlueAsyncClient;
import software.amazon.awssdk.services.glue.GlueAsyncClientBuilder;
import software.amazon.awssdk.services.glue.GlueClient;
import software.amazon.awssdk.services.glue.GlueClientBuilder;
import software.amazon.awssdk.services.greengrass.GreengrassAsyncClient;
import software.amazon.awssdk.services.greengrass.GreengrassAsyncClientBuilder;
import software.amazon.awssdk.services.greengrass.GreengrassClient;
import software.amazon.awssdk.services.greengrass.GreengrassClientBuilder;
import software.amazon.awssdk.services.greengrassv2.GreengrassV2AsyncClient;
import software.amazon.awssdk.services.greengrassv2.GreengrassV2AsyncClientBuilder;
import software.amazon.awssdk.services.greengrassv2.GreengrassV2Client;
import software.amazon.awssdk.services.greengrassv2.GreengrassV2ClientBuilder;
import software.amazon.awssdk.services.groundstation.GroundStationAsyncClient;
import software.amazon.awssdk.services.groundstation.GroundStationAsyncClientBuilder;
import software.amazon.awssdk.services.groundstation.GroundStationClient;
import software.amazon.awssdk.services.groundstation.GroundStationClientBuilder;
import software.amazon.awssdk.services.guardduty.GuardDutyAsyncClient;
import software.amazon.awssdk.services.guardduty.GuardDutyAsyncClientBuilder;
import software.amazon.awssdk.services.guardduty.GuardDutyClient;
import software.amazon.awssdk.services.guardduty.GuardDutyClientBuilder;
import software.amazon.awssdk.services.health.HealthAsyncClient;
import software.amazon.awssdk.services.health.HealthAsyncClientBuilder;
import software.amazon.awssdk.services.health.HealthClient;
import software.amazon.awssdk.services.health.HealthClientBuilder;
import software.amazon.awssdk.services.healthlake.HealthLakeAsyncClient;
import software.amazon.awssdk.services.healthlake.HealthLakeAsyncClientBuilder;
import software.amazon.awssdk.services.healthlake.HealthLakeClient;
import software.amazon.awssdk.services.healthlake.HealthLakeClientBuilder;
import software.amazon.awssdk.services.honeycode.HoneycodeAsyncClient;
import software.amazon.awssdk.services.honeycode.HoneycodeAsyncClientBuilder;
import software.amazon.awssdk.services.honeycode.HoneycodeClient;
import software.amazon.awssdk.services.honeycode.HoneycodeClientBuilder;
import software.amazon.awssdk.services.iam.IamAsyncClient;
import software.amazon.awssdk.services.iam.IamAsyncClientBuilder;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.IamClientBuilder;
import software.amazon.awssdk.services.identitystore.IdentitystoreAsyncClient;
import software.amazon.awssdk.services.identitystore.IdentitystoreAsyncClientBuilder;
import software.amazon.awssdk.services.identitystore.IdentitystoreClient;
import software.amazon.awssdk.services.identitystore.IdentitystoreClientBuilder;
import software.amazon.awssdk.services.imagebuilder.ImagebuilderAsyncClient;
import software.amazon.awssdk.services.imagebuilder.ImagebuilderAsyncClientBuilder;
import software.amazon.awssdk.services.imagebuilder.ImagebuilderClient;
import software.amazon.awssdk.services.imagebuilder.ImagebuilderClientBuilder;
import software.amazon.awssdk.services.inspector.InspectorAsyncClient;
import software.amazon.awssdk.services.inspector.InspectorAsyncClientBuilder;
import software.amazon.awssdk.services.inspector.InspectorClient;
import software.amazon.awssdk.services.inspector.InspectorClientBuilder;
import software.amazon.awssdk.services.iot1clickdevices.Iot1ClickDevicesAsyncClient;
import software.amazon.awssdk.services.iot1clickdevices.Iot1ClickDevicesAsyncClientBuilder;
import software.amazon.awssdk.services.iot1clickdevices.Iot1ClickDevicesClient;
import software.amazon.awssdk.services.iot1clickdevices.Iot1ClickDevicesClientBuilder;
import software.amazon.awssdk.services.iot1clickprojects.Iot1ClickProjectsAsyncClient;
import software.amazon.awssdk.services.iot1clickprojects.Iot1ClickProjectsAsyncClientBuilder;
import software.amazon.awssdk.services.iot1clickprojects.Iot1ClickProjectsClient;
import software.amazon.awssdk.services.iot1clickprojects.Iot1ClickProjectsClientBuilder;
import software.amazon.awssdk.services.iotanalytics.IoTAnalyticsAsyncClient;
import software.amazon.awssdk.services.iotanalytics.IoTAnalyticsAsyncClientBuilder;
import software.amazon.awssdk.services.iotanalytics.IoTAnalyticsClient;
import software.amazon.awssdk.services.iotanalytics.IoTAnalyticsClientBuilder;
import software.amazon.awssdk.services.iot.IotAsyncClient;
import software.amazon.awssdk.services.iot.IotAsyncClientBuilder;
import software.amazon.awssdk.services.iot.IotClient;
import software.amazon.awssdk.services.iot.IotClientBuilder;
import software.amazon.awssdk.services.iotdataplane.IotDataPlaneAsyncClient;
import software.amazon.awssdk.services.iotdataplane.IotDataPlaneAsyncClientBuilder;
import software.amazon.awssdk.services.iotdataplane.IotDataPlaneClient;
import software.amazon.awssdk.services.iotdataplane.IotDataPlaneClientBuilder;
import software.amazon.awssdk.services.iotdeviceadvisor.IotDeviceAdvisorAsyncClient;
import software.amazon.awssdk.services.iotdeviceadvisor.IotDeviceAdvisorAsyncClientBuilder;
import software.amazon.awssdk.services.iotdeviceadvisor.IotDeviceAdvisorClient;
import software.amazon.awssdk.services.iotdeviceadvisor.IotDeviceAdvisorClientBuilder;
import software.amazon.awssdk.services.iotevents.IotEventsAsyncClient;
import software.amazon.awssdk.services.iotevents.IotEventsAsyncClientBuilder;
import software.amazon.awssdk.services.iotevents.IotEventsClient;
import software.amazon.awssdk.services.iotevents.IotEventsClientBuilder;
import software.amazon.awssdk.services.ioteventsdata.IotEventsDataAsyncClient;
import software.amazon.awssdk.services.ioteventsdata.IotEventsDataAsyncClientBuilder;
import software.amazon.awssdk.services.ioteventsdata.IotEventsDataClient;
import software.amazon.awssdk.services.ioteventsdata.IotEventsDataClientBuilder;
import software.amazon.awssdk.services.iotfleethub.IoTFleetHubAsyncClient;
import software.amazon.awssdk.services.iotfleethub.IoTFleetHubAsyncClientBuilder;
import software.amazon.awssdk.services.iotfleethub.IoTFleetHubClient;
import software.amazon.awssdk.services.iotfleethub.IoTFleetHubClientBuilder;
import software.amazon.awssdk.services.iotjobsdataplane.IotJobsDataPlaneAsyncClient;
import software.amazon.awssdk.services.iotjobsdataplane.IotJobsDataPlaneAsyncClientBuilder;
import software.amazon.awssdk.services.iotjobsdataplane.IotJobsDataPlaneClient;
import software.amazon.awssdk.services.iotjobsdataplane.IotJobsDataPlaneClientBuilder;
import software.amazon.awssdk.services.iotsecuretunneling.IoTSecureTunnelingAsyncClient;
import software.amazon.awssdk.services.iotsecuretunneling.IoTSecureTunnelingAsyncClientBuilder;
import software.amazon.awssdk.services.iotsecuretunneling.IoTSecureTunnelingClient;
import software.amazon.awssdk.services.iotsecuretunneling.IoTSecureTunnelingClientBuilder;
import software.amazon.awssdk.services.iotsitewise.IoTSiteWiseAsyncClient;
import software.amazon.awssdk.services.iotsitewise.IoTSiteWiseAsyncClientBuilder;
import software.amazon.awssdk.services.iotsitewise.IoTSiteWiseClient;
import software.amazon.awssdk.services.iotsitewise.IoTSiteWiseClientBuilder;
import software.amazon.awssdk.services.iotthingsgraph.IoTThingsGraphAsyncClient;
import software.amazon.awssdk.services.iotthingsgraph.IoTThingsGraphAsyncClientBuilder;
import software.amazon.awssdk.services.iotthingsgraph.IoTThingsGraphClient;
import software.amazon.awssdk.services.iotthingsgraph.IoTThingsGraphClientBuilder;
import software.amazon.awssdk.services.iotwireless.IotWirelessAsyncClient;
import software.amazon.awssdk.services.iotwireless.IotWirelessAsyncClientBuilder;
import software.amazon.awssdk.services.iotwireless.IotWirelessClient;
import software.amazon.awssdk.services.iotwireless.IotWirelessClientBuilder;
import software.amazon.awssdk.services.ivs.IvsAsyncClient;
import software.amazon.awssdk.services.ivs.IvsAsyncClientBuilder;
import software.amazon.awssdk.services.ivs.IvsClient;
import software.amazon.awssdk.services.ivs.IvsClientBuilder;
import software.amazon.awssdk.services.kafka.KafkaAsyncClient;
import software.amazon.awssdk.services.kafka.KafkaAsyncClientBuilder;
import software.amazon.awssdk.services.kafka.KafkaClient;
import software.amazon.awssdk.services.kafka.KafkaClientBuilder;
import software.amazon.awssdk.services.kendra.KendraAsyncClient;
import software.amazon.awssdk.services.kendra.KendraAsyncClientBuilder;
import software.amazon.awssdk.services.kendra.KendraClient;
import software.amazon.awssdk.services.kendra.KendraClientBuilder;
import software.amazon.awssdk.services.kinesisanalytics.KinesisAnalyticsAsyncClient;
import software.amazon.awssdk.services.kinesisanalytics.KinesisAnalyticsAsyncClientBuilder;
import software.amazon.awssdk.services.kinesisanalytics.KinesisAnalyticsClient;
import software.amazon.awssdk.services.kinesisanalytics.KinesisAnalyticsClientBuilder;
import software.amazon.awssdk.services.kinesisanalyticsv2.KinesisAnalyticsV2AsyncClient;
import software.amazon.awssdk.services.kinesisanalyticsv2.KinesisAnalyticsV2AsyncClientBuilder;
import software.amazon.awssdk.services.kinesisanalyticsv2.KinesisAnalyticsV2Client;
import software.amazon.awssdk.services.kinesisanalyticsv2.KinesisAnalyticsV2ClientBuilder;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClientBuilder;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.KinesisClientBuilder;
import software.amazon.awssdk.services.kinesisvideoarchivedmedia.KinesisVideoArchivedMediaAsyncClient;
import software.amazon.awssdk.services.kinesisvideoarchivedmedia.KinesisVideoArchivedMediaAsyncClientBuilder;
import software.amazon.awssdk.services.kinesisvideoarchivedmedia.KinesisVideoArchivedMediaClient;
import software.amazon.awssdk.services.kinesisvideoarchivedmedia.KinesisVideoArchivedMediaClientBuilder;
import software.amazon.awssdk.services.kinesisvideo.KinesisVideoAsyncClient;
import software.amazon.awssdk.services.kinesisvideo.KinesisVideoAsyncClientBuilder;
import software.amazon.awssdk.services.kinesisvideo.KinesisVideoClient;
import software.amazon.awssdk.services.kinesisvideo.KinesisVideoClientBuilder;
import software.amazon.awssdk.services.kinesisvideomedia.KinesisVideoMediaAsyncClient;
import software.amazon.awssdk.services.kinesisvideomedia.KinesisVideoMediaAsyncClientBuilder;
import software.amazon.awssdk.services.kinesisvideomedia.KinesisVideoMediaClient;
import software.amazon.awssdk.services.kinesisvideomedia.KinesisVideoMediaClientBuilder;
import software.amazon.awssdk.services.kinesisvideosignaling.KinesisVideoSignalingAsyncClient;
import software.amazon.awssdk.services.kinesisvideosignaling.KinesisVideoSignalingAsyncClientBuilder;
import software.amazon.awssdk.services.kinesisvideosignaling.KinesisVideoSignalingClient;
import software.amazon.awssdk.services.kinesisvideosignaling.KinesisVideoSignalingClientBuilder;
import software.amazon.awssdk.services.kms.KmsAsyncClient;
import software.amazon.awssdk.services.kms.KmsAsyncClientBuilder;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.KmsClientBuilder;
import software.amazon.awssdk.services.lakeformation.LakeFormationAsyncClient;
import software.amazon.awssdk.services.lakeformation.LakeFormationAsyncClientBuilder;
import software.amazon.awssdk.services.lakeformation.LakeFormationClient;
import software.amazon.awssdk.services.lakeformation.LakeFormationClientBuilder;
import software.amazon.awssdk.services.lambda.LambdaAsyncClient;
import software.amazon.awssdk.services.lambda.LambdaAsyncClientBuilder;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.LambdaClientBuilder;
import software.amazon.awssdk.services.lexmodelbuilding.LexModelBuildingAsyncClient;
import software.amazon.awssdk.services.lexmodelbuilding.LexModelBuildingAsyncClientBuilder;
import software.amazon.awssdk.services.lexmodelbuilding.LexModelBuildingClient;
import software.amazon.awssdk.services.lexmodelbuilding.LexModelBuildingClientBuilder;
import software.amazon.awssdk.services.lexmodelsv2.LexModelsV2AsyncClient;
import software.amazon.awssdk.services.lexmodelsv2.LexModelsV2AsyncClientBuilder;
import software.amazon.awssdk.services.lexmodelsv2.LexModelsV2Client;
import software.amazon.awssdk.services.lexmodelsv2.LexModelsV2ClientBuilder;
import software.amazon.awssdk.services.lexruntime.LexRuntimeAsyncClient;
import software.amazon.awssdk.services.lexruntime.LexRuntimeAsyncClientBuilder;
import software.amazon.awssdk.services.lexruntime.LexRuntimeClient;
import software.amazon.awssdk.services.lexruntime.LexRuntimeClientBuilder;
import software.amazon.awssdk.services.lexruntimev2.LexRuntimeV2AsyncClient;
import software.amazon.awssdk.services.lexruntimev2.LexRuntimeV2AsyncClientBuilder;
import software.amazon.awssdk.services.lexruntimev2.LexRuntimeV2Client;
import software.amazon.awssdk.services.lexruntimev2.LexRuntimeV2ClientBuilder;
import software.amazon.awssdk.services.licensemanager.LicenseManagerAsyncClient;
import software.amazon.awssdk.services.licensemanager.LicenseManagerAsyncClientBuilder;
import software.amazon.awssdk.services.licensemanager.LicenseManagerClient;
import software.amazon.awssdk.services.licensemanager.LicenseManagerClientBuilder;
import software.amazon.awssdk.services.lightsail.LightsailAsyncClient;
import software.amazon.awssdk.services.lightsail.LightsailAsyncClientBuilder;
import software.amazon.awssdk.services.lightsail.LightsailClient;
import software.amazon.awssdk.services.lightsail.LightsailClientBuilder;
import software.amazon.awssdk.services.location.LocationAsyncClient;
import software.amazon.awssdk.services.location.LocationAsyncClientBuilder;
import software.amazon.awssdk.services.location.LocationClient;
import software.amazon.awssdk.services.location.LocationClientBuilder;
import software.amazon.awssdk.services.lookoutmetrics.LookoutMetricsAsyncClient;
import software.amazon.awssdk.services.lookoutmetrics.LookoutMetricsAsyncClientBuilder;
import software.amazon.awssdk.services.lookoutmetrics.LookoutMetricsClient;
import software.amazon.awssdk.services.lookoutmetrics.LookoutMetricsClientBuilder;
import software.amazon.awssdk.services.lookoutvision.LookoutVisionAsyncClient;
import software.amazon.awssdk.services.lookoutvision.LookoutVisionAsyncClientBuilder;
import software.amazon.awssdk.services.lookoutvision.LookoutVisionClient;
import software.amazon.awssdk.services.lookoutvision.LookoutVisionClientBuilder;
import software.amazon.awssdk.services.machinelearning.MachineLearningAsyncClient;
import software.amazon.awssdk.services.machinelearning.MachineLearningAsyncClientBuilder;
import software.amazon.awssdk.services.machinelearning.MachineLearningClient;
import software.amazon.awssdk.services.machinelearning.MachineLearningClientBuilder;
import software.amazon.awssdk.services.macie2.Macie2AsyncClient;
import software.amazon.awssdk.services.macie2.Macie2AsyncClientBuilder;
import software.amazon.awssdk.services.macie2.Macie2Client;
import software.amazon.awssdk.services.macie2.Macie2ClientBuilder;
import software.amazon.awssdk.services.macie.MacieAsyncClient;
import software.amazon.awssdk.services.macie.MacieAsyncClientBuilder;
import software.amazon.awssdk.services.macie.MacieClient;
import software.amazon.awssdk.services.macie.MacieClientBuilder;
import software.amazon.awssdk.services.managedblockchain.ManagedBlockchainAsyncClient;
import software.amazon.awssdk.services.managedblockchain.ManagedBlockchainAsyncClientBuilder;
import software.amazon.awssdk.services.managedblockchain.ManagedBlockchainClient;
import software.amazon.awssdk.services.managedblockchain.ManagedBlockchainClientBuilder;
import software.amazon.awssdk.services.marketplacecatalog.MarketplaceCatalogAsyncClient;
import software.amazon.awssdk.services.marketplacecatalog.MarketplaceCatalogAsyncClientBuilder;
import software.amazon.awssdk.services.marketplacecatalog.MarketplaceCatalogClient;
import software.amazon.awssdk.services.marketplacecatalog.MarketplaceCatalogClientBuilder;
import software.amazon.awssdk.services.marketplacecommerceanalytics.MarketplaceCommerceAnalyticsAsyncClient;
import software.amazon.awssdk.services.marketplacecommerceanalytics.MarketplaceCommerceAnalyticsAsyncClientBuilder;
import software.amazon.awssdk.services.marketplacecommerceanalytics.MarketplaceCommerceAnalyticsClient;
import software.amazon.awssdk.services.marketplacecommerceanalytics.MarketplaceCommerceAnalyticsClientBuilder;
import software.amazon.awssdk.services.marketplaceentitlement.MarketplaceEntitlementAsyncClient;
import software.amazon.awssdk.services.marketplaceentitlement.MarketplaceEntitlementAsyncClientBuilder;
import software.amazon.awssdk.services.marketplaceentitlement.MarketplaceEntitlementClient;
import software.amazon.awssdk.services.marketplaceentitlement.MarketplaceEntitlementClientBuilder;
import software.amazon.awssdk.services.marketplacemetering.MarketplaceMeteringAsyncClient;
import software.amazon.awssdk.services.marketplacemetering.MarketplaceMeteringAsyncClientBuilder;
import software.amazon.awssdk.services.marketplacemetering.MarketplaceMeteringClient;
import software.amazon.awssdk.services.marketplacemetering.MarketplaceMeteringClientBuilder;
import software.amazon.awssdk.services.mediaconnect.MediaConnectAsyncClient;
import software.amazon.awssdk.services.mediaconnect.MediaConnectAsyncClientBuilder;
import software.amazon.awssdk.services.mediaconnect.MediaConnectClient;
import software.amazon.awssdk.services.mediaconnect.MediaConnectClientBuilder;
import software.amazon.awssdk.services.mediaconvert.MediaConvertAsyncClient;
import software.amazon.awssdk.services.mediaconvert.MediaConvertAsyncClientBuilder;
import software.amazon.awssdk.services.mediaconvert.MediaConvertClient;
import software.amazon.awssdk.services.mediaconvert.MediaConvertClientBuilder;
import software.amazon.awssdk.services.medialive.MediaLiveAsyncClient;
import software.amazon.awssdk.services.medialive.MediaLiveAsyncClientBuilder;
import software.amazon.awssdk.services.medialive.MediaLiveClient;
import software.amazon.awssdk.services.medialive.MediaLiveClientBuilder;
import software.amazon.awssdk.services.mediapackage.MediaPackageAsyncClient;
import software.amazon.awssdk.services.mediapackage.MediaPackageAsyncClientBuilder;
import software.amazon.awssdk.services.mediapackage.MediaPackageClient;
import software.amazon.awssdk.services.mediapackage.MediaPackageClientBuilder;
import software.amazon.awssdk.services.mediapackagevod.MediaPackageVodAsyncClient;
import software.amazon.awssdk.services.mediapackagevod.MediaPackageVodAsyncClientBuilder;
import software.amazon.awssdk.services.mediapackagevod.MediaPackageVodClient;
import software.amazon.awssdk.services.mediapackagevod.MediaPackageVodClientBuilder;
import software.amazon.awssdk.services.mediastore.MediaStoreAsyncClient;
import software.amazon.awssdk.services.mediastore.MediaStoreAsyncClientBuilder;
import software.amazon.awssdk.services.mediastore.MediaStoreClient;
import software.amazon.awssdk.services.mediastore.MediaStoreClientBuilder;
import software.amazon.awssdk.services.mediastoredata.MediaStoreDataAsyncClient;
import software.amazon.awssdk.services.mediastoredata.MediaStoreDataAsyncClientBuilder;
import software.amazon.awssdk.services.mediastoredata.MediaStoreDataClient;
import software.amazon.awssdk.services.mediastoredata.MediaStoreDataClientBuilder;
import software.amazon.awssdk.services.mediatailor.MediaTailorAsyncClient;
import software.amazon.awssdk.services.mediatailor.MediaTailorAsyncClientBuilder;
import software.amazon.awssdk.services.mediatailor.MediaTailorClient;
import software.amazon.awssdk.services.mediatailor.MediaTailorClientBuilder;
import software.amazon.awssdk.services.migrationhub.MigrationHubAsyncClient;
import software.amazon.awssdk.services.migrationhub.MigrationHubAsyncClientBuilder;
import software.amazon.awssdk.services.migrationhub.MigrationHubClient;
import software.amazon.awssdk.services.migrationhub.MigrationHubClientBuilder;
import software.amazon.awssdk.services.migrationhubconfig.MigrationHubConfigAsyncClient;
import software.amazon.awssdk.services.migrationhubconfig.MigrationHubConfigAsyncClientBuilder;
import software.amazon.awssdk.services.migrationhubconfig.MigrationHubConfigClient;
import software.amazon.awssdk.services.migrationhubconfig.MigrationHubConfigClientBuilder;
import software.amazon.awssdk.services.mobile.MobileAsyncClient;
import software.amazon.awssdk.services.mobile.MobileAsyncClientBuilder;
import software.amazon.awssdk.services.mobile.MobileClient;
import software.amazon.awssdk.services.mobile.MobileClientBuilder;
import software.amazon.awssdk.services.mq.MqAsyncClient;
import software.amazon.awssdk.services.mq.MqAsyncClientBuilder;
import software.amazon.awssdk.services.mq.MqClient;
import software.amazon.awssdk.services.mq.MqClientBuilder;
import software.amazon.awssdk.services.mturk.MTurkAsyncClient;
import software.amazon.awssdk.services.mturk.MTurkAsyncClientBuilder;
import software.amazon.awssdk.services.mturk.MTurkClient;
import software.amazon.awssdk.services.mturk.MTurkClientBuilder;
import software.amazon.awssdk.services.mwaa.MwaaAsyncClient;
import software.amazon.awssdk.services.mwaa.MwaaAsyncClientBuilder;
import software.amazon.awssdk.services.mwaa.MwaaClient;
import software.amazon.awssdk.services.mwaa.MwaaClientBuilder;
import software.amazon.awssdk.services.neptune.NeptuneAsyncClient;
import software.amazon.awssdk.services.neptune.NeptuneAsyncClientBuilder;
import software.amazon.awssdk.services.neptune.NeptuneClient;
import software.amazon.awssdk.services.neptune.NeptuneClientBuilder;
import software.amazon.awssdk.services.networkfirewall.NetworkFirewallAsyncClient;
import software.amazon.awssdk.services.networkfirewall.NetworkFirewallAsyncClientBuilder;
import software.amazon.awssdk.services.networkfirewall.NetworkFirewallClient;
import software.amazon.awssdk.services.networkfirewall.NetworkFirewallClientBuilder;
import software.amazon.awssdk.services.networkmanager.NetworkManagerAsyncClient;
import software.amazon.awssdk.services.networkmanager.NetworkManagerAsyncClientBuilder;
import software.amazon.awssdk.services.networkmanager.NetworkManagerClient;
import software.amazon.awssdk.services.networkmanager.NetworkManagerClientBuilder;
import software.amazon.awssdk.services.opsworks.OpsWorksAsyncClient;
import software.amazon.awssdk.services.opsworks.OpsWorksAsyncClientBuilder;
import software.amazon.awssdk.services.opsworks.OpsWorksClient;
import software.amazon.awssdk.services.opsworks.OpsWorksClientBuilder;
import software.amazon.awssdk.services.opsworkscm.OpsWorksCmAsyncClient;
import software.amazon.awssdk.services.opsworkscm.OpsWorksCmAsyncClientBuilder;
import software.amazon.awssdk.services.opsworkscm.OpsWorksCmClient;
import software.amazon.awssdk.services.opsworkscm.OpsWorksCmClientBuilder;
import software.amazon.awssdk.services.organizations.OrganizationsAsyncClient;
import software.amazon.awssdk.services.organizations.OrganizationsAsyncClientBuilder;
import software.amazon.awssdk.services.organizations.OrganizationsClient;
import software.amazon.awssdk.services.organizations.OrganizationsClientBuilder;
import software.amazon.awssdk.services.outposts.OutpostsAsyncClient;
import software.amazon.awssdk.services.outposts.OutpostsAsyncClientBuilder;
import software.amazon.awssdk.services.outposts.OutpostsClient;
import software.amazon.awssdk.services.outposts.OutpostsClientBuilder;
import software.amazon.awssdk.services.personalize.PersonalizeAsyncClient;
import software.amazon.awssdk.services.personalize.PersonalizeAsyncClientBuilder;
import software.amazon.awssdk.services.personalize.PersonalizeClient;
import software.amazon.awssdk.services.personalize.PersonalizeClientBuilder;
import software.amazon.awssdk.services.personalizeevents.PersonalizeEventsAsyncClient;
import software.amazon.awssdk.services.personalizeevents.PersonalizeEventsAsyncClientBuilder;
import software.amazon.awssdk.services.personalizeevents.PersonalizeEventsClient;
import software.amazon.awssdk.services.personalizeevents.PersonalizeEventsClientBuilder;
import software.amazon.awssdk.services.personalizeruntime.PersonalizeRuntimeAsyncClient;
import software.amazon.awssdk.services.personalizeruntime.PersonalizeRuntimeAsyncClientBuilder;
import software.amazon.awssdk.services.personalizeruntime.PersonalizeRuntimeClient;
import software.amazon.awssdk.services.personalizeruntime.PersonalizeRuntimeClientBuilder;
import software.amazon.awssdk.services.pi.PiAsyncClient;
import software.amazon.awssdk.services.pi.PiAsyncClientBuilder;
import software.amazon.awssdk.services.pi.PiClient;
import software.amazon.awssdk.services.pi.PiClientBuilder;
import software.amazon.awssdk.services.pinpoint.PinpointAsyncClient;
import software.amazon.awssdk.services.pinpoint.PinpointAsyncClientBuilder;
import software.amazon.awssdk.services.pinpoint.PinpointClient;
import software.amazon.awssdk.services.pinpoint.PinpointClientBuilder;
import software.amazon.awssdk.services.pinpointemail.PinpointEmailAsyncClient;
import software.amazon.awssdk.services.pinpointemail.PinpointEmailAsyncClientBuilder;
import software.amazon.awssdk.services.pinpointemail.PinpointEmailClient;
import software.amazon.awssdk.services.pinpointemail.PinpointEmailClientBuilder;
import software.amazon.awssdk.services.pinpointsmsvoice.PinpointSmsVoiceAsyncClient;
import software.amazon.awssdk.services.pinpointsmsvoice.PinpointSmsVoiceAsyncClientBuilder;
import software.amazon.awssdk.services.pinpointsmsvoice.PinpointSmsVoiceClient;
import software.amazon.awssdk.services.pinpointsmsvoice.PinpointSmsVoiceClientBuilder;
import software.amazon.awssdk.services.polly.PollyAsyncClient;
import software.amazon.awssdk.services.polly.PollyAsyncClientBuilder;
import software.amazon.awssdk.services.polly.PollyClient;
import software.amazon.awssdk.services.polly.PollyClientBuilder;
import software.amazon.awssdk.services.pricing.PricingAsyncClient;
import software.amazon.awssdk.services.pricing.PricingAsyncClientBuilder;
import software.amazon.awssdk.services.pricing.PricingClient;
import software.amazon.awssdk.services.pricing.PricingClientBuilder;
import software.amazon.awssdk.services.qldb.QldbAsyncClient;
import software.amazon.awssdk.services.qldb.QldbAsyncClientBuilder;
import software.amazon.awssdk.services.qldb.QldbClient;
import software.amazon.awssdk.services.qldb.QldbClientBuilder;
import software.amazon.awssdk.services.qldbsession.QldbSessionAsyncClient;
import software.amazon.awssdk.services.qldbsession.QldbSessionAsyncClientBuilder;
import software.amazon.awssdk.services.qldbsession.QldbSessionClient;
import software.amazon.awssdk.services.qldbsession.QldbSessionClientBuilder;
import software.amazon.awssdk.services.quicksight.QuickSightAsyncClient;
import software.amazon.awssdk.services.quicksight.QuickSightAsyncClientBuilder;
import software.amazon.awssdk.services.quicksight.QuickSightClient;
import software.amazon.awssdk.services.quicksight.QuickSightClientBuilder;
import software.amazon.awssdk.services.ram.RamAsyncClient;
import software.amazon.awssdk.services.ram.RamAsyncClientBuilder;
import software.amazon.awssdk.services.ram.RamClient;
import software.amazon.awssdk.services.ram.RamClientBuilder;
import software.amazon.awssdk.services.rds.RdsAsyncClient;
import software.amazon.awssdk.services.rds.RdsAsyncClientBuilder;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.RdsClientBuilder;
import software.amazon.awssdk.services.rdsdata.RdsDataAsyncClient;
import software.amazon.awssdk.services.rdsdata.RdsDataAsyncClientBuilder;
import software.amazon.awssdk.services.rdsdata.RdsDataClient;
import software.amazon.awssdk.services.rdsdata.RdsDataClientBuilder;
import software.amazon.awssdk.services.redshift.RedshiftAsyncClient;
import software.amazon.awssdk.services.redshift.RedshiftAsyncClientBuilder;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.RedshiftClientBuilder;
import software.amazon.awssdk.services.redshiftdata.RedshiftDataAsyncClient;
import software.amazon.awssdk.services.redshiftdata.RedshiftDataAsyncClientBuilder;
import software.amazon.awssdk.services.redshiftdata.RedshiftDataClient;
import software.amazon.awssdk.services.redshiftdata.RedshiftDataClientBuilder;
import software.amazon.awssdk.services.rekognition.RekognitionAsyncClient;
import software.amazon.awssdk.services.rekognition.RekognitionAsyncClientBuilder;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.RekognitionClientBuilder;
import software.amazon.awssdk.services.resourcegroups.ResourceGroupsAsyncClient;
import software.amazon.awssdk.services.resourcegroups.ResourceGroupsAsyncClientBuilder;
import software.amazon.awssdk.services.resourcegroups.ResourceGroupsClient;
import software.amazon.awssdk.services.resourcegroups.ResourceGroupsClientBuilder;
import software.amazon.awssdk.services.resourcegroupstaggingapi.ResourceGroupsTaggingApiAsyncClient;
import software.amazon.awssdk.services.resourcegroupstaggingapi.ResourceGroupsTaggingApiAsyncClientBuilder;
import software.amazon.awssdk.services.resourcegroupstaggingapi.ResourceGroupsTaggingApiClient;
import software.amazon.awssdk.services.resourcegroupstaggingapi.ResourceGroupsTaggingApiClientBuilder;
import software.amazon.awssdk.services.robomaker.RoboMakerAsyncClient;
import software.amazon.awssdk.services.robomaker.RoboMakerAsyncClientBuilder;
import software.amazon.awssdk.services.robomaker.RoboMakerClient;
import software.amazon.awssdk.services.robomaker.RoboMakerClientBuilder;
import software.amazon.awssdk.services.route53.Route53AsyncClient;
import software.amazon.awssdk.services.route53.Route53AsyncClientBuilder;
import software.amazon.awssdk.services.route53.Route53Client;
import software.amazon.awssdk.services.route53.Route53ClientBuilder;
import software.amazon.awssdk.services.route53domains.Route53DomainsAsyncClient;
import software.amazon.awssdk.services.route53domains.Route53DomainsAsyncClientBuilder;
import software.amazon.awssdk.services.route53domains.Route53DomainsClient;
import software.amazon.awssdk.services.route53domains.Route53DomainsClientBuilder;
import software.amazon.awssdk.services.route53resolver.Route53ResolverAsyncClient;
import software.amazon.awssdk.services.route53resolver.Route53ResolverAsyncClientBuilder;
import software.amazon.awssdk.services.route53resolver.Route53ResolverClient;
import software.amazon.awssdk.services.route53resolver.Route53ResolverClientBuilder;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3AsyncClientBuilder;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3control.S3ControlAsyncClient;
import software.amazon.awssdk.services.s3control.S3ControlAsyncClientBuilder;
import software.amazon.awssdk.services.s3control.S3ControlClient;
import software.amazon.awssdk.services.s3control.S3ControlClientBuilder;
import software.amazon.awssdk.services.s3outposts.S3OutpostsAsyncClient;
import software.amazon.awssdk.services.s3outposts.S3OutpostsAsyncClientBuilder;
import software.amazon.awssdk.services.s3outposts.S3OutpostsClient;
import software.amazon.awssdk.services.s3outposts.S3OutpostsClientBuilder;
import software.amazon.awssdk.services.sagemakera2iruntime.SageMakerA2IRuntimeAsyncClient;
import software.amazon.awssdk.services.sagemakera2iruntime.SageMakerA2IRuntimeAsyncClientBuilder;
import software.amazon.awssdk.services.sagemakera2iruntime.SageMakerA2IRuntimeClient;
import software.amazon.awssdk.services.sagemakera2iruntime.SageMakerA2IRuntimeClientBuilder;
import software.amazon.awssdk.services.sagemaker.SageMakerAsyncClient;
import software.amazon.awssdk.services.sagemaker.SageMakerAsyncClientBuilder;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.SageMakerClientBuilder;
import software.amazon.awssdk.services.sagemakeredge.SagemakerEdgeAsyncClient;
import software.amazon.awssdk.services.sagemakeredge.SagemakerEdgeAsyncClientBuilder;
import software.amazon.awssdk.services.sagemakeredge.SagemakerEdgeClient;
import software.amazon.awssdk.services.sagemakeredge.SagemakerEdgeClientBuilder;
import software.amazon.awssdk.services.sagemakerfeaturestoreruntime.SageMakerFeatureStoreRuntimeAsyncClient;
import software.amazon.awssdk.services.sagemakerfeaturestoreruntime.SageMakerFeatureStoreRuntimeAsyncClientBuilder;
import software.amazon.awssdk.services.sagemakerfeaturestoreruntime.SageMakerFeatureStoreRuntimeClient;
import software.amazon.awssdk.services.sagemakerfeaturestoreruntime.SageMakerFeatureStoreRuntimeClientBuilder;
import software.amazon.awssdk.services.sagemakerruntime.SageMakerRuntimeAsyncClient;
import software.amazon.awssdk.services.sagemakerruntime.SageMakerRuntimeAsyncClientBuilder;
import software.amazon.awssdk.services.sagemakerruntime.SageMakerRuntimeClient;
import software.amazon.awssdk.services.sagemakerruntime.SageMakerRuntimeClientBuilder;
import software.amazon.awssdk.services.savingsplans.SavingsplansAsyncClient;
import software.amazon.awssdk.services.savingsplans.SavingsplansAsyncClientBuilder;
import software.amazon.awssdk.services.savingsplans.SavingsplansClient;
import software.amazon.awssdk.services.savingsplans.SavingsplansClientBuilder;
import software.amazon.awssdk.services.schemas.SchemasAsyncClient;
import software.amazon.awssdk.services.schemas.SchemasAsyncClientBuilder;
import software.amazon.awssdk.services.schemas.SchemasClient;
import software.amazon.awssdk.services.schemas.SchemasClientBuilder;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerAsyncClient;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerAsyncClientBuilder;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClientBuilder;
import software.amazon.awssdk.services.securityhub.SecurityHubAsyncClient;
import software.amazon.awssdk.services.securityhub.SecurityHubAsyncClientBuilder;
import software.amazon.awssdk.services.securityhub.SecurityHubClient;
import software.amazon.awssdk.services.securityhub.SecurityHubClientBuilder;
import software.amazon.awssdk.services.serverlessapplicationrepository.ServerlessApplicationRepositoryAsyncClient;
import software.amazon.awssdk.services.serverlessapplicationrepository.ServerlessApplicationRepositoryAsyncClientBuilder;
import software.amazon.awssdk.services.serverlessapplicationrepository.ServerlessApplicationRepositoryClient;
import software.amazon.awssdk.services.serverlessapplicationrepository.ServerlessApplicationRepositoryClientBuilder;
import software.amazon.awssdk.services.servicecatalogappregistry.ServiceCatalogAppRegistryAsyncClient;
import software.amazon.awssdk.services.servicecatalogappregistry.ServiceCatalogAppRegistryAsyncClientBuilder;
import software.amazon.awssdk.services.servicecatalogappregistry.ServiceCatalogAppRegistryClient;
import software.amazon.awssdk.services.servicecatalogappregistry.ServiceCatalogAppRegistryClientBuilder;
import software.amazon.awssdk.services.servicecatalog.ServiceCatalogAsyncClient;
import software.amazon.awssdk.services.servicecatalog.ServiceCatalogAsyncClientBuilder;
import software.amazon.awssdk.services.servicecatalog.ServiceCatalogClient;
import software.amazon.awssdk.services.servicecatalog.ServiceCatalogClientBuilder;
import software.amazon.awssdk.services.servicediscovery.ServiceDiscoveryAsyncClient;
import software.amazon.awssdk.services.servicediscovery.ServiceDiscoveryAsyncClientBuilder;
import software.amazon.awssdk.services.servicediscovery.ServiceDiscoveryClient;
import software.amazon.awssdk.services.servicediscovery.ServiceDiscoveryClientBuilder;
import software.amazon.awssdk.services.servicequotas.ServiceQuotasAsyncClient;
import software.amazon.awssdk.services.servicequotas.ServiceQuotasAsyncClientBuilder;
import software.amazon.awssdk.services.servicequotas.ServiceQuotasClient;
import software.amazon.awssdk.services.servicequotas.ServiceQuotasClientBuilder;
import software.amazon.awssdk.services.ses.SesAsyncClient;
import software.amazon.awssdk.services.ses.SesAsyncClientBuilder;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.SesClientBuilder;
import software.amazon.awssdk.services.sesv2.SesV2AsyncClient;
import software.amazon.awssdk.services.sesv2.SesV2AsyncClientBuilder;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.SesV2ClientBuilder;
import software.amazon.awssdk.services.sfn.SfnAsyncClient;
import software.amazon.awssdk.services.sfn.SfnAsyncClientBuilder;
import software.amazon.awssdk.services.sfn.SfnClient;
import software.amazon.awssdk.services.sfn.SfnClientBuilder;
import software.amazon.awssdk.services.shield.ShieldAsyncClient;
import software.amazon.awssdk.services.shield.ShieldAsyncClientBuilder;
import software.amazon.awssdk.services.shield.ShieldClient;
import software.amazon.awssdk.services.shield.ShieldClientBuilder;
import software.amazon.awssdk.services.signer.SignerAsyncClient;
import software.amazon.awssdk.services.signer.SignerAsyncClientBuilder;
import software.amazon.awssdk.services.signer.SignerClient;
import software.amazon.awssdk.services.signer.SignerClientBuilder;
import software.amazon.awssdk.services.sms.SmsAsyncClient;
import software.amazon.awssdk.services.sms.SmsAsyncClientBuilder;
import software.amazon.awssdk.services.sms.SmsClient;
import software.amazon.awssdk.services.sms.SmsClientBuilder;
import software.amazon.awssdk.services.snowball.SnowballAsyncClient;
import software.amazon.awssdk.services.snowball.SnowballAsyncClientBuilder;
import software.amazon.awssdk.services.snowball.SnowballClient;
import software.amazon.awssdk.services.snowball.SnowballClientBuilder;
import software.amazon.awssdk.services.sns.SnsAsyncClient;
import software.amazon.awssdk.services.sns.SnsAsyncClientBuilder;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.SnsClientBuilder;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.SqsAsyncClientBuilder;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.SqsClientBuilder;
import software.amazon.awssdk.services.ssoadmin.SsoAdminAsyncClient;
import software.amazon.awssdk.services.ssoadmin.SsoAdminAsyncClientBuilder;
import software.amazon.awssdk.services.ssoadmin.SsoAdminClient;
import software.amazon.awssdk.services.ssoadmin.SsoAdminClientBuilder;
import software.amazon.awssdk.services.sso.SsoAsyncClient;
import software.amazon.awssdk.services.sso.SsoAsyncClientBuilder;
import software.amazon.awssdk.services.sso.SsoClient;
import software.amazon.awssdk.services.sso.SsoClientBuilder;
import software.amazon.awssdk.services.ssooidc.SsoOidcAsyncClient;
import software.amazon.awssdk.services.ssooidc.SsoOidcAsyncClientBuilder;
import software.amazon.awssdk.services.ssooidc.SsoOidcClient;
import software.amazon.awssdk.services.ssooidc.SsoOidcClientBuilder;
import software.amazon.awssdk.services.storagegateway.StorageGatewayAsyncClient;
import software.amazon.awssdk.services.storagegateway.StorageGatewayAsyncClientBuilder;
import software.amazon.awssdk.services.storagegateway.StorageGatewayClient;
import software.amazon.awssdk.services.storagegateway.StorageGatewayClientBuilder;
import software.amazon.awssdk.services.sts.StsAsyncClient;
import software.amazon.awssdk.services.sts.StsAsyncClientBuilder;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.StsClientBuilder;
import software.amazon.awssdk.services.support.SupportAsyncClient;
import software.amazon.awssdk.services.support.SupportAsyncClientBuilder;
import software.amazon.awssdk.services.support.SupportClient;
import software.amazon.awssdk.services.support.SupportClientBuilder;
import software.amazon.awssdk.services.swf.SwfAsyncClient;
import software.amazon.awssdk.services.swf.SwfAsyncClientBuilder;
import software.amazon.awssdk.services.swf.SwfClient;
import software.amazon.awssdk.services.swf.SwfClientBuilder;
import software.amazon.awssdk.services.synthetics.SyntheticsAsyncClient;
import software.amazon.awssdk.services.synthetics.SyntheticsAsyncClientBuilder;
import software.amazon.awssdk.services.synthetics.SyntheticsClient;
import software.amazon.awssdk.services.synthetics.SyntheticsClientBuilder;
import software.amazon.awssdk.services.textract.TextractAsyncClient;
import software.amazon.awssdk.services.textract.TextractAsyncClientBuilder;
import software.amazon.awssdk.services.textract.TextractClient;
import software.amazon.awssdk.services.textract.TextractClientBuilder;
import software.amazon.awssdk.services.timestreamquery.TimestreamQueryAsyncClient;
import software.amazon.awssdk.services.timestreamquery.TimestreamQueryAsyncClientBuilder;
import software.amazon.awssdk.services.timestreamquery.TimestreamQueryClient;
import software.amazon.awssdk.services.timestreamquery.TimestreamQueryClientBuilder;
import software.amazon.awssdk.services.timestreamwrite.TimestreamWriteAsyncClient;
import software.amazon.awssdk.services.timestreamwrite.TimestreamWriteAsyncClientBuilder;
import software.amazon.awssdk.services.timestreamwrite.TimestreamWriteClient;
import software.amazon.awssdk.services.timestreamwrite.TimestreamWriteClientBuilder;
import software.amazon.awssdk.services.transcribe.TranscribeAsyncClient;
import software.amazon.awssdk.services.transcribe.TranscribeAsyncClientBuilder;
import software.amazon.awssdk.services.transcribe.TranscribeClient;
import software.amazon.awssdk.services.transcribe.TranscribeClientBuilder;
import software.amazon.awssdk.services.transfer.TransferAsyncClient;
import software.amazon.awssdk.services.transfer.TransferAsyncClientBuilder;
import software.amazon.awssdk.services.transfer.TransferClient;
import software.amazon.awssdk.services.transfer.TransferClientBuilder;
import software.amazon.awssdk.services.translate.TranslateAsyncClient;
import software.amazon.awssdk.services.translate.TranslateAsyncClientBuilder;
import software.amazon.awssdk.services.translate.TranslateClient;
import software.amazon.awssdk.services.translate.TranslateClientBuilder;
import software.amazon.awssdk.services.waf.WafAsyncClient;
import software.amazon.awssdk.services.waf.WafAsyncClientBuilder;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.WafClientBuilder;
import software.amazon.awssdk.services.waf.regional.WafRegionalAsyncClient;
import software.amazon.awssdk.services.waf.regional.WafRegionalAsyncClientBuilder;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;
import software.amazon.awssdk.services.waf.regional.WafRegionalClientBuilder;
import software.amazon.awssdk.services.wafv2.Wafv2AsyncClient;
import software.amazon.awssdk.services.wafv2.Wafv2AsyncClientBuilder;
import software.amazon.awssdk.services.wafv2.Wafv2Client;
import software.amazon.awssdk.services.wafv2.Wafv2ClientBuilder;
import software.amazon.awssdk.services.wellarchitected.WellArchitectedAsyncClient;
import software.amazon.awssdk.services.wellarchitected.WellArchitectedAsyncClientBuilder;
import software.amazon.awssdk.services.wellarchitected.WellArchitectedClient;
import software.amazon.awssdk.services.wellarchitected.WellArchitectedClientBuilder;
import software.amazon.awssdk.services.workdocs.WorkDocsAsyncClient;
import software.amazon.awssdk.services.workdocs.WorkDocsAsyncClientBuilder;
import software.amazon.awssdk.services.workdocs.WorkDocsClient;
import software.amazon.awssdk.services.workdocs.WorkDocsClientBuilder;
import software.amazon.awssdk.services.worklink.WorkLinkAsyncClient;
import software.amazon.awssdk.services.worklink.WorkLinkAsyncClientBuilder;
import software.amazon.awssdk.services.worklink.WorkLinkClient;
import software.amazon.awssdk.services.worklink.WorkLinkClientBuilder;
import software.amazon.awssdk.services.workmail.WorkMailAsyncClient;
import software.amazon.awssdk.services.workmail.WorkMailAsyncClientBuilder;
import software.amazon.awssdk.services.workmail.WorkMailClient;
import software.amazon.awssdk.services.workmail.WorkMailClientBuilder;
import software.amazon.awssdk.services.workmailmessageflow.WorkMailMessageFlowAsyncClient;
import software.amazon.awssdk.services.workmailmessageflow.WorkMailMessageFlowAsyncClientBuilder;
import software.amazon.awssdk.services.workmailmessageflow.WorkMailMessageFlowClient;
import software.amazon.awssdk.services.workmailmessageflow.WorkMailMessageFlowClientBuilder;
import software.amazon.awssdk.services.workspaces.WorkSpacesAsyncClient;
import software.amazon.awssdk.services.workspaces.WorkSpacesAsyncClientBuilder;
import software.amazon.awssdk.services.workspaces.WorkSpacesClient;
import software.amazon.awssdk.services.workspaces.WorkSpacesClientBuilder;
import software.amazon.awssdk.services.xray.XRayAsyncClient;
import software.amazon.awssdk.services.xray.XRayAsyncClientBuilder;
import software.amazon.awssdk.services.xray.XRayClient;
import software.amazon.awssdk.services.xray.XRayClientBuilder;

import javax.inject.Singleton;


@SdkClient(client = AccessAnalyzerClient.class, clientBuilder = AccessAnalyzerClientBuilder.class, asyncClient = AccessAnalyzerAsyncClient.class, asyncClientBuilder = AccessAnalyzerAsyncClientBuilder.class)
@SdkClient(client = AcmClient.class, clientBuilder = AcmClientBuilder.class, asyncClient = AcmAsyncClient.class, asyncClientBuilder = AcmAsyncClientBuilder.class)
@SdkClient(client = AcmPcaClient.class, clientBuilder = AcmPcaClientBuilder.class, asyncClient = AcmPcaAsyncClient.class, asyncClientBuilder = AcmPcaAsyncClientBuilder.class)
@SdkClient(client = AlexaForBusinessClient.class, clientBuilder = AlexaForBusinessClientBuilder.class, asyncClient = AlexaForBusinessAsyncClient.class, asyncClientBuilder = AlexaForBusinessAsyncClientBuilder.class)
@SdkClient(client = AmpClient.class, clientBuilder = AmpClientBuilder.class, asyncClient = AmpAsyncClient.class, asyncClientBuilder = AmpAsyncClientBuilder.class)
@SdkClient(client = AmplifyClient.class, clientBuilder = AmplifyClientBuilder.class, asyncClient = AmplifyAsyncClient.class, asyncClientBuilder = AmplifyAsyncClientBuilder.class)
@SdkClient(client = AmplifyBackendClient.class, clientBuilder = AmplifyBackendClientBuilder.class, asyncClient = AmplifyBackendAsyncClient.class, asyncClientBuilder = AmplifyBackendAsyncClientBuilder.class)
@SdkClient(client = ApiGatewayClient.class, clientBuilder = ApiGatewayClientBuilder.class, asyncClient = ApiGatewayAsyncClient.class, asyncClientBuilder = ApiGatewayAsyncClientBuilder.class)
@SdkClient(client = ApiGatewayManagementApiClient.class, clientBuilder = ApiGatewayManagementApiClientBuilder.class, asyncClient = ApiGatewayManagementApiAsyncClient.class, asyncClientBuilder = ApiGatewayManagementApiAsyncClientBuilder.class)
@SdkClient(client = ApiGatewayV2Client.class, clientBuilder = ApiGatewayV2ClientBuilder.class, asyncClient = ApiGatewayV2AsyncClient.class, asyncClientBuilder = ApiGatewayV2AsyncClientBuilder.class)
@SdkClient(client = AppConfigClient.class, clientBuilder = AppConfigClientBuilder.class, asyncClient = AppConfigAsyncClient.class, asyncClientBuilder = AppConfigAsyncClientBuilder.class)
@SdkClient(client = AppflowClient.class, clientBuilder = AppflowClientBuilder.class, asyncClient = AppflowAsyncClient.class, asyncClientBuilder = AppflowAsyncClientBuilder.class)
@SdkClient(client = AppIntegrationsClient.class, clientBuilder = AppIntegrationsClientBuilder.class, asyncClient = AppIntegrationsAsyncClient.class, asyncClientBuilder = AppIntegrationsAsyncClientBuilder.class)
@SdkClient(client = ApplicationAutoScalingClient.class, clientBuilder = ApplicationAutoScalingClientBuilder.class, asyncClient = ApplicationAutoScalingAsyncClient.class, asyncClientBuilder = ApplicationAutoScalingAsyncClientBuilder.class)
@SdkClient(client = ApplicationDiscoveryClient.class, clientBuilder = ApplicationDiscoveryClientBuilder.class, asyncClient = ApplicationDiscoveryAsyncClient.class, asyncClientBuilder = ApplicationDiscoveryAsyncClientBuilder.class)
@SdkClient(client = ApplicationInsightsClient.class, clientBuilder = ApplicationInsightsClientBuilder.class, asyncClient = ApplicationInsightsAsyncClient.class, asyncClientBuilder = ApplicationInsightsAsyncClientBuilder.class)
@SdkClient(client = AppMeshClient.class, clientBuilder = AppMeshClientBuilder.class, asyncClient = AppMeshAsyncClient.class, asyncClientBuilder = AppMeshAsyncClientBuilder.class)
@SdkClient(client = AppStreamClient.class, clientBuilder = AppStreamClientBuilder.class, asyncClient = AppStreamAsyncClient.class, asyncClientBuilder = AppStreamAsyncClientBuilder.class)
@SdkClient(client = AppSyncClient.class, clientBuilder = AppSyncClientBuilder.class, asyncClient = AppSyncAsyncClient.class, asyncClientBuilder = AppSyncAsyncClientBuilder.class)
@SdkClient(client = AthenaClient.class, clientBuilder = AthenaClientBuilder.class, asyncClient = AthenaAsyncClient.class, asyncClientBuilder = AthenaAsyncClientBuilder.class)
@SdkClient(client = AuditManagerClient.class, clientBuilder = AuditManagerClientBuilder.class, asyncClient = AuditManagerAsyncClient.class, asyncClientBuilder = AuditManagerAsyncClientBuilder.class)
@SdkClient(client = AutoScalingClient.class, clientBuilder = AutoScalingClientBuilder.class, asyncClient = AutoScalingAsyncClient.class, asyncClientBuilder = AutoScalingAsyncClientBuilder.class)
@SdkClient(client = AutoScalingPlansClient.class, clientBuilder = AutoScalingPlansClientBuilder.class, asyncClient = AutoScalingPlansAsyncClient.class, asyncClientBuilder = AutoScalingPlansAsyncClientBuilder.class)
@SdkClient(client = BackupClient.class, clientBuilder = BackupClientBuilder.class, asyncClient = BackupAsyncClient.class, asyncClientBuilder = BackupAsyncClientBuilder.class)
@SdkClient(client = BatchClient.class, clientBuilder = BatchClientBuilder.class, asyncClient = BatchAsyncClient.class, asyncClientBuilder = BatchAsyncClientBuilder.class)
@SdkClient(client = BraketClient.class, clientBuilder = BraketClientBuilder.class, asyncClient = BraketAsyncClient.class, asyncClientBuilder = BraketAsyncClientBuilder.class)
@SdkClient(client = BudgetsClient.class, clientBuilder = BudgetsClientBuilder.class, asyncClient = BudgetsAsyncClient.class, asyncClientBuilder = BudgetsAsyncClientBuilder.class)
@SdkClient(client = ChimeClient.class, clientBuilder = ChimeClientBuilder.class, asyncClient = ChimeAsyncClient.class, asyncClientBuilder = ChimeAsyncClientBuilder.class)
@SdkClient(client = Cloud9Client.class, clientBuilder = Cloud9ClientBuilder.class, asyncClient = Cloud9AsyncClient.class, asyncClientBuilder = Cloud9AsyncClientBuilder.class)
@SdkClient(client = CloudDirectoryClient.class, clientBuilder = CloudDirectoryClientBuilder.class, asyncClient = CloudDirectoryAsyncClient.class, asyncClientBuilder = CloudDirectoryAsyncClientBuilder.class)
@SdkClient(client = CloudFormationClient.class, clientBuilder = CloudFormationClientBuilder.class, asyncClient = CloudFormationAsyncClient.class, asyncClientBuilder = CloudFormationAsyncClientBuilder.class)
@SdkClient(client = CloudFrontClient.class, clientBuilder = CloudFrontClientBuilder.class, asyncClient = CloudFrontAsyncClient.class, asyncClientBuilder = CloudFrontAsyncClientBuilder.class)
@SdkClient(client = CloudHsmClient.class, clientBuilder = CloudHsmClientBuilder.class, asyncClient = CloudHsmAsyncClient.class, asyncClientBuilder = CloudHsmAsyncClientBuilder.class)
@SdkClient(client = CloudHsmV2Client.class, clientBuilder = CloudHsmV2ClientBuilder.class, asyncClient = CloudHsmV2AsyncClient.class, asyncClientBuilder = CloudHsmV2AsyncClientBuilder.class)
@SdkClient(client = CloudSearchClient.class, clientBuilder = CloudSearchClientBuilder.class, asyncClient = CloudSearchAsyncClient.class, asyncClientBuilder = CloudSearchAsyncClientBuilder.class)
@SdkClient(client = CloudSearchDomainClient.class, clientBuilder = CloudSearchDomainClientBuilder.class, asyncClient = CloudSearchDomainAsyncClient.class, asyncClientBuilder = CloudSearchDomainAsyncClientBuilder.class)
@SdkClient(client = CloudTrailClient.class, clientBuilder = CloudTrailClientBuilder.class, asyncClient = CloudTrailAsyncClient.class, asyncClientBuilder = CloudTrailAsyncClientBuilder.class)
@SdkClient(client = CloudWatchClient.class, clientBuilder = CloudWatchClientBuilder.class, asyncClient = CloudWatchAsyncClient.class, asyncClientBuilder = CloudWatchAsyncClientBuilder.class)
@SdkClient(client = CloudWatchEventsClient.class, clientBuilder = CloudWatchEventsClientBuilder.class, asyncClient = CloudWatchEventsAsyncClient.class, asyncClientBuilder = CloudWatchEventsAsyncClientBuilder.class)
@SdkClient(client = CloudWatchLogsClient.class, clientBuilder = CloudWatchLogsClientBuilder.class, asyncClient = CloudWatchLogsAsyncClient.class, asyncClientBuilder = CloudWatchLogsAsyncClientBuilder.class)
@SdkClient(client = CodeartifactClient.class, clientBuilder = CodeartifactClientBuilder.class, asyncClient = CodeartifactAsyncClient.class, asyncClientBuilder = CodeartifactAsyncClientBuilder.class)
@SdkClient(client = CodeBuildClient.class, clientBuilder = CodeBuildClientBuilder.class, asyncClient = CodeBuildAsyncClient.class, asyncClientBuilder = CodeBuildAsyncClientBuilder.class)
@SdkClient(client = CodeCommitClient.class, clientBuilder = CodeCommitClientBuilder.class, asyncClient = CodeCommitAsyncClient.class, asyncClientBuilder = CodeCommitAsyncClientBuilder.class)
@SdkClient(client = CodeDeployClient.class, clientBuilder = CodeDeployClientBuilder.class, asyncClient = CodeDeployAsyncClient.class, asyncClientBuilder = CodeDeployAsyncClientBuilder.class)
@SdkClient(client = CodeGuruProfilerClient.class, clientBuilder = CodeGuruProfilerClientBuilder.class, asyncClient = CodeGuruProfilerAsyncClient.class, asyncClientBuilder = CodeGuruProfilerAsyncClientBuilder.class)
@SdkClient(client = CodeGuruReviewerClient.class, clientBuilder = CodeGuruReviewerClientBuilder.class, asyncClient = CodeGuruReviewerAsyncClient.class, asyncClientBuilder = CodeGuruReviewerAsyncClientBuilder.class)
@SdkClient(client = CodePipelineClient.class, clientBuilder = CodePipelineClientBuilder.class, asyncClient = CodePipelineAsyncClient.class, asyncClientBuilder = CodePipelineAsyncClientBuilder.class)
@SdkClient(client = CodeStarClient.class, clientBuilder = CodeStarClientBuilder.class, asyncClient = CodeStarAsyncClient.class, asyncClientBuilder = CodeStarAsyncClientBuilder.class)
@SdkClient(client = CodeStarConnectionsClient.class, clientBuilder = CodeStarConnectionsClientBuilder.class, asyncClient = CodeStarConnectionsAsyncClient.class, asyncClientBuilder = CodeStarConnectionsAsyncClientBuilder.class)
@SdkClient(client = CodestarNotificationsClient.class, clientBuilder = CodestarNotificationsClientBuilder.class, asyncClient = CodestarNotificationsAsyncClient.class, asyncClientBuilder = CodestarNotificationsAsyncClientBuilder.class)
@SdkClient(client = CognitoIdentityClient.class, clientBuilder = CognitoIdentityClientBuilder.class, asyncClient = CognitoIdentityAsyncClient.class, asyncClientBuilder = CognitoIdentityAsyncClientBuilder.class)
@SdkClient(client = CognitoIdentityProviderClient.class, clientBuilder = CognitoIdentityProviderClientBuilder.class, asyncClient = CognitoIdentityProviderAsyncClient.class, asyncClientBuilder = CognitoIdentityProviderAsyncClientBuilder.class)
@SdkClient(client = CognitoSyncClient.class, clientBuilder = CognitoSyncClientBuilder.class, asyncClient = CognitoSyncAsyncClient.class, asyncClientBuilder = CognitoSyncAsyncClientBuilder.class)
@SdkClient(client = ComprehendClient.class, clientBuilder = ComprehendClientBuilder.class, asyncClient = ComprehendAsyncClient.class, asyncClientBuilder = ComprehendAsyncClientBuilder.class)
@SdkClient(client = ComprehendMedicalClient.class, clientBuilder = ComprehendMedicalClientBuilder.class, asyncClient = ComprehendMedicalAsyncClient.class, asyncClientBuilder = ComprehendMedicalAsyncClientBuilder.class)
@SdkClient(client = ComputeOptimizerClient.class, clientBuilder = ComputeOptimizerClientBuilder.class, asyncClient = ComputeOptimizerAsyncClient.class, asyncClientBuilder = ComputeOptimizerAsyncClientBuilder.class)
@SdkClient(client = ConfigClient.class, clientBuilder = ConfigClientBuilder.class, asyncClient = ConfigAsyncClient.class, asyncClientBuilder = ConfigAsyncClientBuilder.class)
@SdkClient(client = ConnectClient.class, clientBuilder = ConnectClientBuilder.class, asyncClient = ConnectAsyncClient.class, asyncClientBuilder = ConnectAsyncClientBuilder.class)
@SdkClient(client = ConnectContactLensClient.class, clientBuilder = ConnectContactLensClientBuilder.class, asyncClient = ConnectContactLensAsyncClient.class, asyncClientBuilder = ConnectContactLensAsyncClientBuilder.class)
@SdkClient(client = ConnectParticipantClient.class, clientBuilder = ConnectParticipantClientBuilder.class, asyncClient = ConnectParticipantAsyncClient.class, asyncClientBuilder = ConnectParticipantAsyncClientBuilder.class)
@SdkClient(client = CostAndUsageReportClient.class, clientBuilder = CostAndUsageReportClientBuilder.class, asyncClient = CostAndUsageReportAsyncClient.class, asyncClientBuilder = CostAndUsageReportAsyncClientBuilder.class)
@SdkClient(client = CostExplorerClient.class, clientBuilder = CostExplorerClientBuilder.class, asyncClient = CostExplorerAsyncClient.class, asyncClientBuilder = CostExplorerAsyncClientBuilder.class)
@SdkClient(client = CustomerProfilesClient.class, clientBuilder = CustomerProfilesClientBuilder.class, asyncClient = CustomerProfilesAsyncClient.class, asyncClientBuilder = CustomerProfilesAsyncClientBuilder.class)
@SdkClient(client = DatabaseMigrationClient.class, clientBuilder = DatabaseMigrationClientBuilder.class, asyncClient = DatabaseMigrationAsyncClient.class, asyncClientBuilder = DatabaseMigrationAsyncClientBuilder.class)
@SdkClient(client = DataBrewClient.class, clientBuilder = DataBrewClientBuilder.class, asyncClient = DataBrewAsyncClient.class, asyncClientBuilder = DataBrewAsyncClientBuilder.class)
@SdkClient(client = DataExchangeClient.class, clientBuilder = DataExchangeClientBuilder.class, asyncClient = DataExchangeAsyncClient.class, asyncClientBuilder = DataExchangeAsyncClientBuilder.class)
@SdkClient(client = DataPipelineClient.class, clientBuilder = DataPipelineClientBuilder.class, asyncClient = DataPipelineAsyncClient.class, asyncClientBuilder = DataPipelineAsyncClientBuilder.class)
@SdkClient(client = DataSyncClient.class, clientBuilder = DataSyncClientBuilder.class, asyncClient = DataSyncAsyncClient.class, asyncClientBuilder = DataSyncAsyncClientBuilder.class)
@SdkClient(client = DaxClient.class, clientBuilder = DaxClientBuilder.class, asyncClient = DaxAsyncClient.class, asyncClientBuilder = DaxAsyncClientBuilder.class)
@SdkClient(client = DetectiveClient.class, clientBuilder = DetectiveClientBuilder.class, asyncClient = DetectiveAsyncClient.class, asyncClientBuilder = DetectiveAsyncClientBuilder.class)
@SdkClient(client = DeviceFarmClient.class, clientBuilder = DeviceFarmClientBuilder.class, asyncClient = DeviceFarmAsyncClient.class, asyncClientBuilder = DeviceFarmAsyncClientBuilder.class)
@SdkClient(client = DevOpsGuruClient.class, clientBuilder = DevOpsGuruClientBuilder.class, asyncClient = DevOpsGuruAsyncClient.class, asyncClientBuilder = DevOpsGuruAsyncClientBuilder.class)
@SdkClient(client = DirectConnectClient.class, clientBuilder = DirectConnectClientBuilder.class, asyncClient = DirectConnectAsyncClient.class, asyncClientBuilder = DirectConnectAsyncClientBuilder.class)
@SdkClient(client = DirectoryClient.class, clientBuilder = DirectoryClientBuilder.class, asyncClient = DirectoryAsyncClient.class, asyncClientBuilder = DirectoryAsyncClientBuilder.class)
@SdkClient(client = DlmClient.class, clientBuilder = DlmClientBuilder.class, asyncClient = DlmAsyncClient.class, asyncClientBuilder = DlmAsyncClientBuilder.class)
@SdkClient(client = DocDbClient.class, clientBuilder = DocDbClientBuilder.class, asyncClient = DocDbAsyncClient.class, asyncClientBuilder = DocDbAsyncClientBuilder.class)
@SdkClient(client = DynamoDbClient.class, clientBuilder = DynamoDbClientBuilder.class, asyncClient = DynamoDbAsyncClient.class, asyncClientBuilder = DynamoDbAsyncClientBuilder.class)
@SdkClient(client = DynamoDbStreamsClient.class, clientBuilder = DynamoDbStreamsClientBuilder.class, asyncClient = DynamoDbStreamsAsyncClient.class, asyncClientBuilder = DynamoDbStreamsAsyncClientBuilder.class)
@SdkClient(client = EbsClient.class, clientBuilder = EbsClientBuilder.class, asyncClient = EbsAsyncClient.class, asyncClientBuilder = EbsAsyncClientBuilder.class)
@SdkClient(client = Ec2Client.class, clientBuilder = Ec2ClientBuilder.class, asyncClient = Ec2AsyncClient.class, asyncClientBuilder = Ec2AsyncClientBuilder.class)
@SdkClient(client = Ec2InstanceConnectClient.class, clientBuilder = Ec2InstanceConnectClientBuilder.class, asyncClient = Ec2InstanceConnectAsyncClient.class, asyncClientBuilder = Ec2InstanceConnectAsyncClientBuilder.class)
@SdkClient(client = EcrClient.class, clientBuilder = EcrClientBuilder.class, asyncClient = EcrAsyncClient.class, asyncClientBuilder = EcrAsyncClientBuilder.class)
@SdkClient(client = EcrPublicClient.class, clientBuilder = EcrPublicClientBuilder.class, asyncClient = EcrPublicAsyncClient.class, asyncClientBuilder = EcrPublicAsyncClientBuilder.class)
@SdkClient(client = EcsClient.class, clientBuilder = EcsClientBuilder.class, asyncClient = EcsAsyncClient.class, asyncClientBuilder = EcsAsyncClientBuilder.class)
@SdkClient(client = EfsClient.class, clientBuilder = EfsClientBuilder.class, asyncClient = EfsAsyncClient.class, asyncClientBuilder = EfsAsyncClientBuilder.class)
@SdkClient(client = EksClient.class, clientBuilder = EksClientBuilder.class, asyncClient = EksAsyncClient.class, asyncClientBuilder = EksAsyncClientBuilder.class)
@SdkClient(client = ElastiCacheClient.class, clientBuilder = ElastiCacheClientBuilder.class, asyncClient = ElastiCacheAsyncClient.class, asyncClientBuilder = ElastiCacheAsyncClientBuilder.class)
@SdkClient(client = ElasticBeanstalkClient.class, clientBuilder = ElasticBeanstalkClientBuilder.class, asyncClient = ElasticBeanstalkAsyncClient.class, asyncClientBuilder = ElasticBeanstalkAsyncClientBuilder.class)
@SdkClient(client = ElasticInferenceClient.class, clientBuilder = ElasticInferenceClientBuilder.class, asyncClient = ElasticInferenceAsyncClient.class, asyncClientBuilder = ElasticInferenceAsyncClientBuilder.class)
@SdkClient(client = ElasticLoadBalancingClient.class, clientBuilder = ElasticLoadBalancingClientBuilder.class, asyncClient = ElasticLoadBalancingAsyncClient.class, asyncClientBuilder = ElasticLoadBalancingAsyncClientBuilder.class)
@SdkClient(client = ElasticLoadBalancingV2Client.class, clientBuilder = ElasticLoadBalancingV2ClientBuilder.class, asyncClient = ElasticLoadBalancingV2AsyncClient.class, asyncClientBuilder = ElasticLoadBalancingV2AsyncClientBuilder.class)
@SdkClient(client = ElasticsearchClient.class, clientBuilder = ElasticsearchClientBuilder.class, asyncClient = ElasticsearchAsyncClient.class, asyncClientBuilder = ElasticsearchAsyncClientBuilder.class)
@SdkClient(client = ElasticTranscoderClient.class, clientBuilder = ElasticTranscoderClientBuilder.class, asyncClient = ElasticTranscoderAsyncClient.class, asyncClientBuilder = ElasticTranscoderAsyncClientBuilder.class)
@SdkClient(client = EmrClient.class, clientBuilder = EmrClientBuilder.class, asyncClient = EmrAsyncClient.class, asyncClientBuilder = EmrAsyncClientBuilder.class)
@SdkClient(client = EmrContainersClient.class, clientBuilder = EmrContainersClientBuilder.class, asyncClient = EmrContainersAsyncClient.class, asyncClientBuilder = EmrContainersAsyncClientBuilder.class)
@SdkClient(client = EventBridgeClient.class, clientBuilder = EventBridgeClientBuilder.class, asyncClient = EventBridgeAsyncClient.class, asyncClientBuilder = EventBridgeAsyncClientBuilder.class)
@SdkClient(client = FirehoseClient.class, clientBuilder = FirehoseClientBuilder.class, asyncClient = FirehoseAsyncClient.class, asyncClientBuilder = FirehoseAsyncClientBuilder.class)
@SdkClient(client = FisClient.class, clientBuilder = FisClientBuilder.class, asyncClient = FisAsyncClient.class, asyncClientBuilder = FisAsyncClientBuilder.class)
@SdkClient(client = FmsClient.class, clientBuilder = FmsClientBuilder.class, asyncClient = FmsAsyncClient.class, asyncClientBuilder = FmsAsyncClientBuilder.class)
@SdkClient(client = ForecastClient.class, clientBuilder = ForecastClientBuilder.class, asyncClient = ForecastAsyncClient.class, asyncClientBuilder = ForecastAsyncClientBuilder.class)
@SdkClient(client = ForecastqueryClient.class, clientBuilder = ForecastqueryClientBuilder.class, asyncClient = ForecastqueryAsyncClient.class, asyncClientBuilder = ForecastqueryAsyncClientBuilder.class)
@SdkClient(client = FraudDetectorClient.class, clientBuilder = FraudDetectorClientBuilder.class, asyncClient = FraudDetectorAsyncClient.class, asyncClientBuilder = FraudDetectorAsyncClientBuilder.class)
@SdkClient(client = FSxClient.class, clientBuilder = FSxClientBuilder.class, asyncClient = FSxAsyncClient.class, asyncClientBuilder = FSxAsyncClientBuilder.class)
@SdkClient(client = GameLiftClient.class, clientBuilder = GameLiftClientBuilder.class, asyncClient = GameLiftAsyncClient.class, asyncClientBuilder = GameLiftAsyncClientBuilder.class)
@SdkClient(client = GlacierClient.class, clientBuilder = GlacierClientBuilder.class, asyncClient = GlacierAsyncClient.class, asyncClientBuilder = GlacierAsyncClientBuilder.class)
@SdkClient(client = GlobalAcceleratorClient.class, clientBuilder = GlobalAcceleratorClientBuilder.class, asyncClient = GlobalAcceleratorAsyncClient.class, asyncClientBuilder = GlobalAcceleratorAsyncClientBuilder.class)
@SdkClient(client = GlueClient.class, clientBuilder = GlueClientBuilder.class, asyncClient = GlueAsyncClient.class, asyncClientBuilder = GlueAsyncClientBuilder.class)
@SdkClient(client = GreengrassClient.class, clientBuilder = GreengrassClientBuilder.class, asyncClient = GreengrassAsyncClient.class, asyncClientBuilder = GreengrassAsyncClientBuilder.class)
@SdkClient(client = GreengrassV2Client.class, clientBuilder = GreengrassV2ClientBuilder.class, asyncClient = GreengrassV2AsyncClient.class, asyncClientBuilder = GreengrassV2AsyncClientBuilder.class)
@SdkClient(client = GroundStationClient.class, clientBuilder = GroundStationClientBuilder.class, asyncClient = GroundStationAsyncClient.class, asyncClientBuilder = GroundStationAsyncClientBuilder.class)
@SdkClient(client = GuardDutyClient.class, clientBuilder = GuardDutyClientBuilder.class, asyncClient = GuardDutyAsyncClient.class, asyncClientBuilder = GuardDutyAsyncClientBuilder.class)
@SdkClient(client = HealthClient.class, clientBuilder = HealthClientBuilder.class, asyncClient = HealthAsyncClient.class, asyncClientBuilder = HealthAsyncClientBuilder.class)
@SdkClient(client = HealthLakeClient.class, clientBuilder = HealthLakeClientBuilder.class, asyncClient = HealthLakeAsyncClient.class, asyncClientBuilder = HealthLakeAsyncClientBuilder.class)
@SdkClient(client = HoneycodeClient.class, clientBuilder = HoneycodeClientBuilder.class, asyncClient = HoneycodeAsyncClient.class, asyncClientBuilder = HoneycodeAsyncClientBuilder.class)
@SdkClient(client = IamClient.class, clientBuilder = IamClientBuilder.class, asyncClient = IamAsyncClient.class, asyncClientBuilder = IamAsyncClientBuilder.class)
@SdkClient(client = IdentitystoreClient.class, clientBuilder = IdentitystoreClientBuilder.class, asyncClient = IdentitystoreAsyncClient.class, asyncClientBuilder = IdentitystoreAsyncClientBuilder.class)
@SdkClient(client = ImagebuilderClient.class, clientBuilder = ImagebuilderClientBuilder.class, asyncClient = ImagebuilderAsyncClient.class, asyncClientBuilder = ImagebuilderAsyncClientBuilder.class)
@SdkClient(client = InspectorClient.class, clientBuilder = InspectorClientBuilder.class, asyncClient = InspectorAsyncClient.class, asyncClientBuilder = InspectorAsyncClientBuilder.class)
@SdkClient(client = Iot1ClickDevicesClient.class, clientBuilder = Iot1ClickDevicesClientBuilder.class, asyncClient = Iot1ClickDevicesAsyncClient.class, asyncClientBuilder = Iot1ClickDevicesAsyncClientBuilder.class)
@SdkClient(client = Iot1ClickProjectsClient.class, clientBuilder = Iot1ClickProjectsClientBuilder.class, asyncClient = Iot1ClickProjectsAsyncClient.class, asyncClientBuilder = Iot1ClickProjectsAsyncClientBuilder.class)
@SdkClient(client = IoTAnalyticsClient.class, clientBuilder = IoTAnalyticsClientBuilder.class, asyncClient = IoTAnalyticsAsyncClient.class, asyncClientBuilder = IoTAnalyticsAsyncClientBuilder.class)
@SdkClient(client = IotClient.class, clientBuilder = IotClientBuilder.class, asyncClient = IotAsyncClient.class, asyncClientBuilder = IotAsyncClientBuilder.class)
@SdkClient(client = IotDataPlaneClient.class, clientBuilder = IotDataPlaneClientBuilder.class, asyncClient = IotDataPlaneAsyncClient.class, asyncClientBuilder = IotDataPlaneAsyncClientBuilder.class)
@SdkClient(client = IotDeviceAdvisorClient.class, clientBuilder = IotDeviceAdvisorClientBuilder.class, asyncClient = IotDeviceAdvisorAsyncClient.class, asyncClientBuilder = IotDeviceAdvisorAsyncClientBuilder.class)
@SdkClient(client = IotEventsClient.class, clientBuilder = IotEventsClientBuilder.class, asyncClient = IotEventsAsyncClient.class, asyncClientBuilder = IotEventsAsyncClientBuilder.class)
@SdkClient(client = IotEventsDataClient.class, clientBuilder = IotEventsDataClientBuilder.class, asyncClient = IotEventsDataAsyncClient.class, asyncClientBuilder = IotEventsDataAsyncClientBuilder.class)
@SdkClient(client = IoTFleetHubClient.class, clientBuilder = IoTFleetHubClientBuilder.class, asyncClient = IoTFleetHubAsyncClient.class, asyncClientBuilder = IoTFleetHubAsyncClientBuilder.class)
@SdkClient(client = IotJobsDataPlaneClient.class, clientBuilder = IotJobsDataPlaneClientBuilder.class, asyncClient = IotJobsDataPlaneAsyncClient.class, asyncClientBuilder = IotJobsDataPlaneAsyncClientBuilder.class)
@SdkClient(client = IoTSecureTunnelingClient.class, clientBuilder = IoTSecureTunnelingClientBuilder.class, asyncClient = IoTSecureTunnelingAsyncClient.class, asyncClientBuilder = IoTSecureTunnelingAsyncClientBuilder.class)
@SdkClient(client = IoTSiteWiseClient.class, clientBuilder = IoTSiteWiseClientBuilder.class, asyncClient = IoTSiteWiseAsyncClient.class, asyncClientBuilder = IoTSiteWiseAsyncClientBuilder.class)
@SdkClient(client = IoTThingsGraphClient.class, clientBuilder = IoTThingsGraphClientBuilder.class, asyncClient = IoTThingsGraphAsyncClient.class, asyncClientBuilder = IoTThingsGraphAsyncClientBuilder.class)
@SdkClient(client = IotWirelessClient.class, clientBuilder = IotWirelessClientBuilder.class, asyncClient = IotWirelessAsyncClient.class, asyncClientBuilder = IotWirelessAsyncClientBuilder.class)
@SdkClient(client = IvsClient.class, clientBuilder = IvsClientBuilder.class, asyncClient = IvsAsyncClient.class, asyncClientBuilder = IvsAsyncClientBuilder.class)
@SdkClient(client = KafkaClient.class, clientBuilder = KafkaClientBuilder.class, asyncClient = KafkaAsyncClient.class, asyncClientBuilder = KafkaAsyncClientBuilder.class)
@SdkClient(client = KendraClient.class, clientBuilder = KendraClientBuilder.class, asyncClient = KendraAsyncClient.class, asyncClientBuilder = KendraAsyncClientBuilder.class)
@SdkClient(client = KinesisAnalyticsClient.class, clientBuilder = KinesisAnalyticsClientBuilder.class, asyncClient = KinesisAnalyticsAsyncClient.class, asyncClientBuilder = KinesisAnalyticsAsyncClientBuilder.class)
@SdkClient(client = KinesisAnalyticsV2Client.class, clientBuilder = KinesisAnalyticsV2ClientBuilder.class, asyncClient = KinesisAnalyticsV2AsyncClient.class, asyncClientBuilder = KinesisAnalyticsV2AsyncClientBuilder.class)
@SdkClient(client = KinesisClient.class, clientBuilder = KinesisClientBuilder.class, asyncClient = KinesisAsyncClient.class, asyncClientBuilder = KinesisAsyncClientBuilder.class)
@SdkClient(client = KinesisVideoArchivedMediaClient.class, clientBuilder = KinesisVideoArchivedMediaClientBuilder.class, asyncClient = KinesisVideoArchivedMediaAsyncClient.class, asyncClientBuilder = KinesisVideoArchivedMediaAsyncClientBuilder.class)
@SdkClient(client = KinesisVideoClient.class, clientBuilder = KinesisVideoClientBuilder.class, asyncClient = KinesisVideoAsyncClient.class, asyncClientBuilder = KinesisVideoAsyncClientBuilder.class)
@SdkClient(client = KinesisVideoMediaClient.class, clientBuilder = KinesisVideoMediaClientBuilder.class, asyncClient = KinesisVideoMediaAsyncClient.class, asyncClientBuilder = KinesisVideoMediaAsyncClientBuilder.class)
@SdkClient(client = KinesisVideoSignalingClient.class, clientBuilder = KinesisVideoSignalingClientBuilder.class, asyncClient = KinesisVideoSignalingAsyncClient.class, asyncClientBuilder = KinesisVideoSignalingAsyncClientBuilder.class)
@SdkClient(client = KmsClient.class, clientBuilder = KmsClientBuilder.class, asyncClient = KmsAsyncClient.class, asyncClientBuilder = KmsAsyncClientBuilder.class)
@SdkClient(client = LakeFormationClient.class, clientBuilder = LakeFormationClientBuilder.class, asyncClient = LakeFormationAsyncClient.class, asyncClientBuilder = LakeFormationAsyncClientBuilder.class)
@SdkClient(client = LambdaClient.class, clientBuilder = LambdaClientBuilder.class, asyncClient = LambdaAsyncClient.class, asyncClientBuilder = LambdaAsyncClientBuilder.class)
@SdkClient(client = LexModelBuildingClient.class, clientBuilder = LexModelBuildingClientBuilder.class, asyncClient = LexModelBuildingAsyncClient.class, asyncClientBuilder = LexModelBuildingAsyncClientBuilder.class)
@SdkClient(client = LexModelsV2Client.class, clientBuilder = LexModelsV2ClientBuilder.class, asyncClient = LexModelsV2AsyncClient.class, asyncClientBuilder = LexModelsV2AsyncClientBuilder.class)
@SdkClient(client = LexRuntimeClient.class, clientBuilder = LexRuntimeClientBuilder.class, asyncClient = LexRuntimeAsyncClient.class, asyncClientBuilder = LexRuntimeAsyncClientBuilder.class)
@SdkClient(client = LexRuntimeV2Client.class, clientBuilder = LexRuntimeV2ClientBuilder.class, asyncClient = LexRuntimeV2AsyncClient.class, asyncClientBuilder = LexRuntimeV2AsyncClientBuilder.class)
@SdkClient(client = LicenseManagerClient.class, clientBuilder = LicenseManagerClientBuilder.class, asyncClient = LicenseManagerAsyncClient.class, asyncClientBuilder = LicenseManagerAsyncClientBuilder.class)
@SdkClient(client = LightsailClient.class, clientBuilder = LightsailClientBuilder.class, asyncClient = LightsailAsyncClient.class, asyncClientBuilder = LightsailAsyncClientBuilder.class)
@SdkClient(client = LocationClient.class, clientBuilder = LocationClientBuilder.class, asyncClient = LocationAsyncClient.class, asyncClientBuilder = LocationAsyncClientBuilder.class)
@SdkClient(client = LookoutMetricsClient.class, clientBuilder = LookoutMetricsClientBuilder.class, asyncClient = LookoutMetricsAsyncClient.class, asyncClientBuilder = LookoutMetricsAsyncClientBuilder.class)
@SdkClient(client = LookoutVisionClient.class, clientBuilder = LookoutVisionClientBuilder.class, asyncClient = LookoutVisionAsyncClient.class, asyncClientBuilder = LookoutVisionAsyncClientBuilder.class)
@SdkClient(client = MachineLearningClient.class, clientBuilder = MachineLearningClientBuilder.class, asyncClient = MachineLearningAsyncClient.class, asyncClientBuilder = MachineLearningAsyncClientBuilder.class)
@SdkClient(client = Macie2Client.class, clientBuilder = Macie2ClientBuilder.class, asyncClient = Macie2AsyncClient.class, asyncClientBuilder = Macie2AsyncClientBuilder.class)
@SdkClient(client = MacieClient.class, clientBuilder = MacieClientBuilder.class, asyncClient = MacieAsyncClient.class, asyncClientBuilder = MacieAsyncClientBuilder.class)
@SdkClient(client = ManagedBlockchainClient.class, clientBuilder = ManagedBlockchainClientBuilder.class, asyncClient = ManagedBlockchainAsyncClient.class, asyncClientBuilder = ManagedBlockchainAsyncClientBuilder.class)
@SdkClient(client = MarketplaceCatalogClient.class, clientBuilder = MarketplaceCatalogClientBuilder.class, asyncClient = MarketplaceCatalogAsyncClient.class, asyncClientBuilder = MarketplaceCatalogAsyncClientBuilder.class)
@SdkClient(client = MarketplaceCommerceAnalyticsClient.class, clientBuilder = MarketplaceCommerceAnalyticsClientBuilder.class, asyncClient = MarketplaceCommerceAnalyticsAsyncClient.class, asyncClientBuilder = MarketplaceCommerceAnalyticsAsyncClientBuilder.class)
@SdkClient(client = MarketplaceEntitlementClient.class, clientBuilder = MarketplaceEntitlementClientBuilder.class, asyncClient = MarketplaceEntitlementAsyncClient.class, asyncClientBuilder = MarketplaceEntitlementAsyncClientBuilder.class)
@SdkClient(client = MarketplaceMeteringClient.class, clientBuilder = MarketplaceMeteringClientBuilder.class, asyncClient = MarketplaceMeteringAsyncClient.class, asyncClientBuilder = MarketplaceMeteringAsyncClientBuilder.class)
@SdkClient(client = MediaConnectClient.class, clientBuilder = MediaConnectClientBuilder.class, asyncClient = MediaConnectAsyncClient.class, asyncClientBuilder = MediaConnectAsyncClientBuilder.class)
@SdkClient(client = MediaConvertClient.class, clientBuilder = MediaConvertClientBuilder.class, asyncClient = MediaConvertAsyncClient.class, asyncClientBuilder = MediaConvertAsyncClientBuilder.class)
@SdkClient(client = MediaLiveClient.class, clientBuilder = MediaLiveClientBuilder.class, asyncClient = MediaLiveAsyncClient.class, asyncClientBuilder = MediaLiveAsyncClientBuilder.class)
@SdkClient(client = MediaPackageClient.class, clientBuilder = MediaPackageClientBuilder.class, asyncClient = MediaPackageAsyncClient.class, asyncClientBuilder = MediaPackageAsyncClientBuilder.class)
@SdkClient(client = MediaPackageVodClient.class, clientBuilder = MediaPackageVodClientBuilder.class, asyncClient = MediaPackageVodAsyncClient.class, asyncClientBuilder = MediaPackageVodAsyncClientBuilder.class)
@SdkClient(client = MediaStoreClient.class, clientBuilder = MediaStoreClientBuilder.class, asyncClient = MediaStoreAsyncClient.class, asyncClientBuilder = MediaStoreAsyncClientBuilder.class)
@SdkClient(client = MediaStoreDataClient.class, clientBuilder = MediaStoreDataClientBuilder.class, asyncClient = MediaStoreDataAsyncClient.class, asyncClientBuilder = MediaStoreDataAsyncClientBuilder.class)
@SdkClient(client = MediaTailorClient.class, clientBuilder = MediaTailorClientBuilder.class, asyncClient = MediaTailorAsyncClient.class, asyncClientBuilder = MediaTailorAsyncClientBuilder.class)
@SdkClient(client = MigrationHubClient.class, clientBuilder = MigrationHubClientBuilder.class, asyncClient = MigrationHubAsyncClient.class, asyncClientBuilder = MigrationHubAsyncClientBuilder.class)
@SdkClient(client = MigrationHubConfigClient.class, clientBuilder = MigrationHubConfigClientBuilder.class, asyncClient = MigrationHubConfigAsyncClient.class, asyncClientBuilder = MigrationHubConfigAsyncClientBuilder.class)
@SdkClient(client = MobileClient.class, clientBuilder = MobileClientBuilder.class, asyncClient = MobileAsyncClient.class, asyncClientBuilder = MobileAsyncClientBuilder.class)
@SdkClient(client = MqClient.class, clientBuilder = MqClientBuilder.class, asyncClient = MqAsyncClient.class, asyncClientBuilder = MqAsyncClientBuilder.class)
@SdkClient(client = MTurkClient.class, clientBuilder = MTurkClientBuilder.class, asyncClient = MTurkAsyncClient.class, asyncClientBuilder = MTurkAsyncClientBuilder.class)
@SdkClient(client = MwaaClient.class, clientBuilder = MwaaClientBuilder.class, asyncClient = MwaaAsyncClient.class, asyncClientBuilder = MwaaAsyncClientBuilder.class)
@SdkClient(client = NeptuneClient.class, clientBuilder = NeptuneClientBuilder.class, asyncClient = NeptuneAsyncClient.class, asyncClientBuilder = NeptuneAsyncClientBuilder.class)
@SdkClient(client = NetworkFirewallClient.class, clientBuilder = NetworkFirewallClientBuilder.class, asyncClient = NetworkFirewallAsyncClient.class, asyncClientBuilder = NetworkFirewallAsyncClientBuilder.class)
@SdkClient(client = NetworkManagerClient.class, clientBuilder = NetworkManagerClientBuilder.class, asyncClient = NetworkManagerAsyncClient.class, asyncClientBuilder = NetworkManagerAsyncClientBuilder.class)
@SdkClient(client = OpsWorksClient.class, clientBuilder = OpsWorksClientBuilder.class, asyncClient = OpsWorksAsyncClient.class, asyncClientBuilder = OpsWorksAsyncClientBuilder.class)
@SdkClient(client = OpsWorksCmClient.class, clientBuilder = OpsWorksCmClientBuilder.class, asyncClient = OpsWorksCmAsyncClient.class, asyncClientBuilder = OpsWorksCmAsyncClientBuilder.class)
@SdkClient(client = OrganizationsClient.class, clientBuilder = OrganizationsClientBuilder.class, asyncClient = OrganizationsAsyncClient.class, asyncClientBuilder = OrganizationsAsyncClientBuilder.class)
@SdkClient(client = OutpostsClient.class, clientBuilder = OutpostsClientBuilder.class, asyncClient = OutpostsAsyncClient.class, asyncClientBuilder = OutpostsAsyncClientBuilder.class)
@SdkClient(client = PersonalizeClient.class, clientBuilder = PersonalizeClientBuilder.class, asyncClient = PersonalizeAsyncClient.class, asyncClientBuilder = PersonalizeAsyncClientBuilder.class)
@SdkClient(client = PersonalizeEventsClient.class, clientBuilder = PersonalizeEventsClientBuilder.class, asyncClient = PersonalizeEventsAsyncClient.class, asyncClientBuilder = PersonalizeEventsAsyncClientBuilder.class)
@SdkClient(client = PersonalizeRuntimeClient.class, clientBuilder = PersonalizeRuntimeClientBuilder.class, asyncClient = PersonalizeRuntimeAsyncClient.class, asyncClientBuilder = PersonalizeRuntimeAsyncClientBuilder.class)
@SdkClient(client = PiClient.class, clientBuilder = PiClientBuilder.class, asyncClient = PiAsyncClient.class, asyncClientBuilder = PiAsyncClientBuilder.class)
@SdkClient(client = PinpointClient.class, clientBuilder = PinpointClientBuilder.class, asyncClient = PinpointAsyncClient.class, asyncClientBuilder = PinpointAsyncClientBuilder.class)
@SdkClient(client = PinpointEmailClient.class, clientBuilder = PinpointEmailClientBuilder.class, asyncClient = PinpointEmailAsyncClient.class, asyncClientBuilder = PinpointEmailAsyncClientBuilder.class)
@SdkClient(client = PinpointSmsVoiceClient.class, clientBuilder = PinpointSmsVoiceClientBuilder.class, asyncClient = PinpointSmsVoiceAsyncClient.class, asyncClientBuilder = PinpointSmsVoiceAsyncClientBuilder.class)
@SdkClient(client = PollyClient.class, clientBuilder = PollyClientBuilder.class, asyncClient = PollyAsyncClient.class, asyncClientBuilder = PollyAsyncClientBuilder.class)
@SdkClient(client = PricingClient.class, clientBuilder = PricingClientBuilder.class, asyncClient = PricingAsyncClient.class, asyncClientBuilder = PricingAsyncClientBuilder.class)
@SdkClient(client = QldbClient.class, clientBuilder = QldbClientBuilder.class, asyncClient = QldbAsyncClient.class, asyncClientBuilder = QldbAsyncClientBuilder.class)
@SdkClient(client = QldbSessionClient.class, clientBuilder = QldbSessionClientBuilder.class, asyncClient = QldbSessionAsyncClient.class, asyncClientBuilder = QldbSessionAsyncClientBuilder.class)
@SdkClient(client = QuickSightClient.class, clientBuilder = QuickSightClientBuilder.class, asyncClient = QuickSightAsyncClient.class, asyncClientBuilder = QuickSightAsyncClientBuilder.class)
@SdkClient(client = RamClient.class, clientBuilder = RamClientBuilder.class, asyncClient = RamAsyncClient.class, asyncClientBuilder = RamAsyncClientBuilder.class)
@SdkClient(client = RdsClient.class, clientBuilder = RdsClientBuilder.class, asyncClient = RdsAsyncClient.class, asyncClientBuilder = RdsAsyncClientBuilder.class)
@SdkClient(client = RdsDataClient.class, clientBuilder = RdsDataClientBuilder.class, asyncClient = RdsDataAsyncClient.class, asyncClientBuilder = RdsDataAsyncClientBuilder.class)
@SdkClient(client = RedshiftClient.class, clientBuilder = RedshiftClientBuilder.class, asyncClient = RedshiftAsyncClient.class, asyncClientBuilder = RedshiftAsyncClientBuilder.class)
@SdkClient(client = RedshiftDataClient.class, clientBuilder = RedshiftDataClientBuilder.class, asyncClient = RedshiftDataAsyncClient.class, asyncClientBuilder = RedshiftDataAsyncClientBuilder.class)
@SdkClient(client = RekognitionClient.class, clientBuilder = RekognitionClientBuilder.class, asyncClient = RekognitionAsyncClient.class, asyncClientBuilder = RekognitionAsyncClientBuilder.class)
@SdkClient(client = ResourceGroupsClient.class, clientBuilder = ResourceGroupsClientBuilder.class, asyncClient = ResourceGroupsAsyncClient.class, asyncClientBuilder = ResourceGroupsAsyncClientBuilder.class)
@SdkClient(client = ResourceGroupsTaggingApiClient.class, clientBuilder = ResourceGroupsTaggingApiClientBuilder.class, asyncClient = ResourceGroupsTaggingApiAsyncClient.class, asyncClientBuilder = ResourceGroupsTaggingApiAsyncClientBuilder.class)
@SdkClient(client = RoboMakerClient.class, clientBuilder = RoboMakerClientBuilder.class, asyncClient = RoboMakerAsyncClient.class, asyncClientBuilder = RoboMakerAsyncClientBuilder.class)
@SdkClient(client = Route53Client.class, clientBuilder = Route53ClientBuilder.class, asyncClient = Route53AsyncClient.class, asyncClientBuilder = Route53AsyncClientBuilder.class)
@SdkClient(client = Route53DomainsClient.class, clientBuilder = Route53DomainsClientBuilder.class, asyncClient = Route53DomainsAsyncClient.class, asyncClientBuilder = Route53DomainsAsyncClientBuilder.class)
@SdkClient(client = Route53ResolverClient.class, clientBuilder = Route53ResolverClientBuilder.class, asyncClient = Route53ResolverAsyncClient.class, asyncClientBuilder = Route53ResolverAsyncClientBuilder.class)
@SdkClient(client = S3Client.class, clientBuilder = S3ClientBuilder.class, asyncClient = S3AsyncClient.class, asyncClientBuilder = S3AsyncClientBuilder.class)
@SdkClient(client = S3ControlClient.class, clientBuilder = S3ControlClientBuilder.class, asyncClient = S3ControlAsyncClient.class, asyncClientBuilder = S3ControlAsyncClientBuilder.class)
@SdkClient(client = S3OutpostsClient.class, clientBuilder = S3OutpostsClientBuilder.class, asyncClient = S3OutpostsAsyncClient.class, asyncClientBuilder = S3OutpostsAsyncClientBuilder.class)
@SdkClient(client = SageMakerA2IRuntimeClient.class, clientBuilder = SageMakerA2IRuntimeClientBuilder.class, asyncClient = SageMakerA2IRuntimeAsyncClient.class, asyncClientBuilder = SageMakerA2IRuntimeAsyncClientBuilder.class)
@SdkClient(client = SageMakerClient.class, clientBuilder = SageMakerClientBuilder.class, asyncClient = SageMakerAsyncClient.class, asyncClientBuilder = SageMakerAsyncClientBuilder.class)
@SdkClient(client = SagemakerEdgeClient.class, clientBuilder = SagemakerEdgeClientBuilder.class, asyncClient = SagemakerEdgeAsyncClient.class, asyncClientBuilder = SagemakerEdgeAsyncClientBuilder.class)
@SdkClient(client = SageMakerFeatureStoreRuntimeClient.class, clientBuilder = SageMakerFeatureStoreRuntimeClientBuilder.class, asyncClient = SageMakerFeatureStoreRuntimeAsyncClient.class, asyncClientBuilder = SageMakerFeatureStoreRuntimeAsyncClientBuilder.class)
@SdkClient(client = SageMakerRuntimeClient.class, clientBuilder = SageMakerRuntimeClientBuilder.class, asyncClient = SageMakerRuntimeAsyncClient.class, asyncClientBuilder = SageMakerRuntimeAsyncClientBuilder.class)
@SdkClient(client = SavingsplansClient.class, clientBuilder = SavingsplansClientBuilder.class, asyncClient = SavingsplansAsyncClient.class, asyncClientBuilder = SavingsplansAsyncClientBuilder.class)
@SdkClient(client = SchemasClient.class, clientBuilder = SchemasClientBuilder.class, asyncClient = SchemasAsyncClient.class, asyncClientBuilder = SchemasAsyncClientBuilder.class)
@SdkClient(client = SecretsManagerClient.class, clientBuilder = SecretsManagerClientBuilder.class, asyncClient = SecretsManagerAsyncClient.class, asyncClientBuilder = SecretsManagerAsyncClientBuilder.class)
@SdkClient(client = SecurityHubClient.class, clientBuilder = SecurityHubClientBuilder.class, asyncClient = SecurityHubAsyncClient.class, asyncClientBuilder = SecurityHubAsyncClientBuilder.class)
@SdkClient(client = ServerlessApplicationRepositoryClient.class, clientBuilder = ServerlessApplicationRepositoryClientBuilder.class, asyncClient = ServerlessApplicationRepositoryAsyncClient.class, asyncClientBuilder = ServerlessApplicationRepositoryAsyncClientBuilder.class)
@SdkClient(client = ServiceCatalogAppRegistryClient.class, clientBuilder = ServiceCatalogAppRegistryClientBuilder.class, asyncClient = ServiceCatalogAppRegistryAsyncClient.class, asyncClientBuilder = ServiceCatalogAppRegistryAsyncClientBuilder.class)
@SdkClient(client = ServiceCatalogClient.class, clientBuilder = ServiceCatalogClientBuilder.class, asyncClient = ServiceCatalogAsyncClient.class, asyncClientBuilder = ServiceCatalogAsyncClientBuilder.class)
@SdkClient(client = ServiceDiscoveryClient.class, clientBuilder = ServiceDiscoveryClientBuilder.class, asyncClient = ServiceDiscoveryAsyncClient.class, asyncClientBuilder = ServiceDiscoveryAsyncClientBuilder.class)
@SdkClient(client = ServiceQuotasClient.class, clientBuilder = ServiceQuotasClientBuilder.class, asyncClient = ServiceQuotasAsyncClient.class, asyncClientBuilder = ServiceQuotasAsyncClientBuilder.class)
@SdkClient(client = SesClient.class, clientBuilder = SesClientBuilder.class, asyncClient = SesAsyncClient.class, asyncClientBuilder = SesAsyncClientBuilder.class)
@SdkClient(client = SesV2Client.class, clientBuilder = SesV2ClientBuilder.class, asyncClient = SesV2AsyncClient.class, asyncClientBuilder = SesV2AsyncClientBuilder.class)
@SdkClient(client = SfnClient.class, clientBuilder = SfnClientBuilder.class, asyncClient = SfnAsyncClient.class, asyncClientBuilder = SfnAsyncClientBuilder.class)
@SdkClient(client = ShieldClient.class, clientBuilder = ShieldClientBuilder.class, asyncClient = ShieldAsyncClient.class, asyncClientBuilder = ShieldAsyncClientBuilder.class)
@SdkClient(client = SignerClient.class, clientBuilder = SignerClientBuilder.class, asyncClient = SignerAsyncClient.class, asyncClientBuilder = SignerAsyncClientBuilder.class)
@SdkClient(client = SmsClient.class, clientBuilder = SmsClientBuilder.class, asyncClient = SmsAsyncClient.class, asyncClientBuilder = SmsAsyncClientBuilder.class)
@SdkClient(client = SnowballClient.class, clientBuilder = SnowballClientBuilder.class, asyncClient = SnowballAsyncClient.class, asyncClientBuilder = SnowballAsyncClientBuilder.class)
@SdkClient(client = SnsClient.class, clientBuilder = SnsClientBuilder.class, asyncClient = SnsAsyncClient.class, asyncClientBuilder = SnsAsyncClientBuilder.class)
@SdkClient(client = SqsClient.class, clientBuilder = SqsClientBuilder.class, asyncClient = SqsAsyncClient.class, asyncClientBuilder = SqsAsyncClientBuilder.class)
@SdkClient(client = SsoAdminClient.class, clientBuilder = SsoAdminClientBuilder.class, asyncClient = SsoAdminAsyncClient.class, asyncClientBuilder = SsoAdminAsyncClientBuilder.class)
@SdkClient(client = SsoClient.class, clientBuilder = SsoClientBuilder.class, asyncClient = SsoAsyncClient.class, asyncClientBuilder = SsoAsyncClientBuilder.class)
@SdkClient(client = SsoOidcClient.class, clientBuilder = SsoOidcClientBuilder.class, asyncClient = SsoOidcAsyncClient.class, asyncClientBuilder = SsoOidcAsyncClientBuilder.class)
@SdkClient(client = StorageGatewayClient.class, clientBuilder = StorageGatewayClientBuilder.class, asyncClient = StorageGatewayAsyncClient.class, asyncClientBuilder = StorageGatewayAsyncClientBuilder.class)
@SdkClient(client = StsClient.class, clientBuilder = StsClientBuilder.class, asyncClient = StsAsyncClient.class, asyncClientBuilder = StsAsyncClientBuilder.class)
@SdkClient(client = SupportClient.class, clientBuilder = SupportClientBuilder.class, asyncClient = SupportAsyncClient.class, asyncClientBuilder = SupportAsyncClientBuilder.class)
@SdkClient(client = SwfClient.class, clientBuilder = SwfClientBuilder.class, asyncClient = SwfAsyncClient.class, asyncClientBuilder = SwfAsyncClientBuilder.class)
@SdkClient(client = SyntheticsClient.class, clientBuilder = SyntheticsClientBuilder.class, asyncClient = SyntheticsAsyncClient.class, asyncClientBuilder = SyntheticsAsyncClientBuilder.class)
@SdkClient(client = TextractClient.class, clientBuilder = TextractClientBuilder.class, asyncClient = TextractAsyncClient.class, asyncClientBuilder = TextractAsyncClientBuilder.class)
@SdkClient(client = TimestreamQueryClient.class, clientBuilder = TimestreamQueryClientBuilder.class, asyncClient = TimestreamQueryAsyncClient.class, asyncClientBuilder = TimestreamQueryAsyncClientBuilder.class)
@SdkClient(client = TimestreamWriteClient.class, clientBuilder = TimestreamWriteClientBuilder.class, asyncClient = TimestreamWriteAsyncClient.class, asyncClientBuilder = TimestreamWriteAsyncClientBuilder.class)
@SdkClient(client = TranscribeClient.class, clientBuilder = TranscribeClientBuilder.class, asyncClient = TranscribeAsyncClient.class, asyncClientBuilder = TranscribeAsyncClientBuilder.class)
@SdkClient(client = TransferClient.class, clientBuilder = TransferClientBuilder.class, asyncClient = TransferAsyncClient.class, asyncClientBuilder = TransferAsyncClientBuilder.class)
@SdkClient(client = TranslateClient.class, clientBuilder = TranslateClientBuilder.class, asyncClient = TranslateAsyncClient.class, asyncClientBuilder = TranslateAsyncClientBuilder.class)
@SdkClient(client = WafClient.class, clientBuilder = WafClientBuilder.class, asyncClient = WafAsyncClient.class, asyncClientBuilder = WafAsyncClientBuilder.class)
@SdkClient(client = WafRegionalClient.class, clientBuilder = WafRegionalClientBuilder.class, asyncClient = WafRegionalAsyncClient.class, asyncClientBuilder = WafRegionalAsyncClientBuilder.class)
@SdkClient(client = Wafv2Client.class, clientBuilder = Wafv2ClientBuilder.class, asyncClient = Wafv2AsyncClient.class, asyncClientBuilder = Wafv2AsyncClientBuilder.class)
@SdkClient(client = WellArchitectedClient.class, clientBuilder = WellArchitectedClientBuilder.class, asyncClient = WellArchitectedAsyncClient.class, asyncClientBuilder = WellArchitectedAsyncClientBuilder.class)
@SdkClient(client = WorkDocsClient.class, clientBuilder = WorkDocsClientBuilder.class, asyncClient = WorkDocsAsyncClient.class, asyncClientBuilder = WorkDocsAsyncClientBuilder.class)
@SdkClient(client = WorkLinkClient.class, clientBuilder = WorkLinkClientBuilder.class, asyncClient = WorkLinkAsyncClient.class, asyncClientBuilder = WorkLinkAsyncClientBuilder.class)
@SdkClient(client = WorkMailClient.class, clientBuilder = WorkMailClientBuilder.class, asyncClient = WorkMailAsyncClient.class, asyncClientBuilder = WorkMailAsyncClientBuilder.class)
@SdkClient(client = WorkMailMessageFlowClient.class, clientBuilder = WorkMailMessageFlowClientBuilder.class, asyncClient = WorkMailMessageFlowAsyncClient.class, asyncClientBuilder = WorkMailMessageFlowAsyncClientBuilder.class)
@SdkClient(client = WorkSpacesClient.class, clientBuilder = WorkSpacesClientBuilder.class, asyncClient = WorkSpacesAsyncClient.class, asyncClientBuilder = WorkSpacesAsyncClientBuilder.class)
@SdkClient(client = XRayClient.class, clientBuilder = XRayClientBuilder.class, asyncClient = XRayAsyncClient.class, asyncClientBuilder = XRayAsyncClientBuilder.class)
@Singleton
@Internal
final class SdkClientsAutomaticFeatureMetadata {
}
