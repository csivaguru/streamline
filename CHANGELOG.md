## 0.1.4
 * IOT-392: ClusterConfiguration should be able to take in ambari url and grab Storm, Kafka , HDFS , Hbase, Hive etc.. configurations
 * IOT-441: Remove creating tables as part of server start
 * IOT-396: Enhance UDF support in rules engine.
 * IOT-359: Move Windowing its own component.
 * IOT-390: Added option to directly specify conditions in rule api.
 * IOT-429: Topology Editor component panel changes.
 * IOT-425: Topology Editor Improvements.
 * IOT-408: Remove the extra config property in topology level config json.
 * IOT-407: Remove imageFile field for custom processors.
 * IOT-389: A few fixes for issues found while running with mysql db
 * IOT-373: Email Notifier throws IllegalStateException
 * IOT-391: Add Eventhub source example in topology rest api script
 * IOT-397: UI bug fixes and changes for module refactoring 
 * IOT-398: Fix topology dag generation issue due to processor output streams not being populated
 * IOT-326: TopologyDagVisitor to have visit methods for IotasSource/IotasSink/IotasProcessor
 * IOT-368: Send empty array response in GET ALL requests.
 * IOT-389: Added missing ruleinfo and udf tables.
 * IOT-387: Misc fixes including Harsha's fixes to be able to deploy topologies.
 * IOT-287: UI Refactoring
 * IOT-356: Add Kafka, Kinesis, EventHubSpout as sources
 * IOT-369: Make InMemoryStorageManager getNextId thread safe
 * IOT-352: Remove core module and a couple other JIRAs
 * IOT-347: Fix bootstrap.sh so it can be run from anywhere
 * IOT-352: Refactor core module to move components into streams and other modules
 * IOT_344: Iotas build fails at storage-atlas with error
 * IOT-285: Hierarchical tagging system should be independent registry service
 * IOT-279: Add clean up logic if any part of custom processor upload fails.
 * IOT-346: Remove metrics dependency from webservice.
 * IOT-343: UI Sets Parallelism Property to Type String Which Causes Topology Deployment Failure.
 * IOT-266: Added OpenTsdb sink implementation using Storm's OpenTsdb bolt.
 * IOT-311: Support specifying output stream within source/processor apis
 * IOT-342: Rename strings in streams-layout-storm module
 * IOT-295: Move classes to streams-runtime-storm
 * IOT-294: Refactor classes from layout to streams-runtime
 * IOT-341: Build failing due to unit test failures in storage-atlas module
 * IOT-340: Move Cache to a separate module
 * IOT-315: RulesChannelHandler does not override abstract method setSource
 * IOT-317: Create streams-sdk
 * IOT-282: Atlas storage manager implementation
 * IOT-293: Streams: create streams-service
 * IOT-280: catalog refactoring
 * IOT-297: Move metrics to the streams project
 * IOT-307: Create streams-layout-storm
 * IOT-292: Create streams-layout
 * IOT-306: Create new storage module for refactoring 
 * IOT 296: Refactor notifers and notification modules to move to the streams project
 * IOT-289: Streams: create a top level streams and the mvn sub project skelton
 * IOT-267: Intermittent Unit test failure in WindowRulesBoltTest
 * IOT-304: integrate normalization components with new topology DAG APIs
 * IOT-273: Iotas rule processor api sql support
 * IOT-303: Notifications and existing action in rules should use new Action abstraction introduced as part of split/join implementation.
 * IOT-302: split/join processor to be integrated with topology dag component APIs
 * IOT-301: Integrate split - stage - join processor in topology editor
 * IOT-299: Add 'null' check for OpenTSDBWithStormQuerier
 * IOT-188: Added split/join/stage implementation for runtime
 * IOT-274: Introduce REST API: Time-series DB querier
 * IOT-155: Convert Topology UI JSON to Design time entities and construct DAG
 * IOT-161: REST api for source, sink, processor and edge
 * IOT-119: IOT Cache Requirements
 
## 0.1.3
 * IOT-269: Normalization processor runtime and UI integration
 * IOT-239: TP-3 release bug fixes
 * IOT-247: Add integration test for tagging
 * IOT-8: Sliding Time Windows
 * IOT-252: Add integration test for StreamInfo storage with JDBC provider
 * IOT-261: topology status API can't determine status of topology on Storm
 * IOT-262: Fix tagging api throwing java.lang.StackOverflowError: null
 * IOT-259: Do not throw an exception if schema is null
 * IOT-256: Hierarchical tagging UI integration
 * IOT-255: UI integration to improve parser upload experience
 * IOT-265: Topologies with notification sinks throw error while deploying to storm 
 * IOT-248: Integration tests for Notification Service
 * IOT-189: Support adding custom artifacts in storm.jar
 * IOT-251: Multiple parser bolts can over write the same loaded parser jars leading to classloading issues.
 * IOT-250: REST api for output streams
 * IOT-158: UI should provide functionality to select a shuffle
 
## 0.1.2
 * IOT-226: Create file resource(including jars) utility to be used in any component of a topology
 * IOT-238: Fixed Tag and TagStorableMapping to be created as Storables in JDBC/Phoenix storgae providers
 * IOT-196: Phoenix storage provider support in webservices modules.
 * IOT-231: Change device id and version to make and model.
 * IOT-215: Introduce new REST API to show metrics for topology.
 * IOT-170: Add file watcher and custom processor file watcher handler with unit tests.
 * IOT-224: Create tags on the fly for datasources for backward compatibility.
 * IOT-201: Integrate storm-sql UDF in Iotas.
 * IOT-99 : Implement hierarchical tags for IoTaS
 * IOT-116: Parser Exception handling requirements.
 * IOT-192: Added support for templatized message.
 * IOT-172: Fix parser file name conflict.
 * IOT-206: Generate normalization processor related components required for flux yaml and UI.
 * IOT-86: Added api to query topology status.
 * IOT-191: UI - topology actions should be integrated and provide view mode and edit mode on topology editor
 * IOT-210: UI - Drop down for configs should be just move to Control Panel
 * IOT-211: UI - Add breadcrumbs navigation bar for all the pages.
 * IOT-199: UI - Improve the componen icon status to show its not configured
 * IOT-217: Refactor to keep source, sink and processor interfaces independent
 * IOT-173: Parser upload should throw a error back to user if the specified class is not available in the jar.
 * IOT-174: Improve Parser upload experience.
 * IOT-203: Make configKey to not be a user input for hbase bolt.
 * IOT-176: topology editor deploy fails if stormHomeDir lacks leading /
 * IOT-147: Support array and nested field lookup with Storm Sql.
 * IOT-177: Email notification bolt is throwing IllegalStateException.
 * IOT-175: Fix generics in Parser.
 * IOT-64:  Added normalization processor for bulk/field level normalization with groovy scripts.
 * IOT-171: Normalization processor/bolt contract

## 0.1.1
 * IOT-135: Implement Custom Processors.
 * IOT-131: Added common Config object.
 * IOT-128: Refactored design time entities.
 * IOT-148: Topology Editor should be able delete the nodes.
 * IOT-98: Add pair of parser name and version unique constraint in parser info.
 * IOT-149: Topology Editor should allow multiples rules through UI.
 * IOT-154: Fix package names to comply with the pattern com.hortonworks.iotas.
 * IOT-32:  Make Logging frameworks coherent across libraries and IOT codebase.
 * IOT-63:  scriptEngine.eval(scriptText) is run every tuple which is heavy and it should be avoided.
 * IOT-78:  Add dataset(weather, twittter firehouse.. etc) to IoT.
 * IOT-112: DummyRuleBolt should be renamed and moved to test package.
 * IOT-113: Typos in error message thrown while deploying topology.
 * IOT-133: create iotas-dist, cleanup artifactid, start/stop script.
 * IOT-137: Support shuffle mechanisms based on stream processing framework.
 * IOT-152: Fix rpm build issues.
 * IOT-159: Topology Editor changes.
 * IOT-162: UI - connecting Parser to sink should be possible.
 * IOT-163: UI - Any modal thats open should be closed by esc key.
 * IOT-178: UI - Support Nested Json.
 * IOT-179: UI - Enable custom processors registration and using them in topology editor.
 * IOT-195: Custom Processor in topology editor.
