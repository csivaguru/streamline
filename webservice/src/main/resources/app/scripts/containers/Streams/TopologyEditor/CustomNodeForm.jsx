import React, {Component, PropTypes}from 'react';
import ReactDOM from 'react-dom';
import _ from 'lodash';
import Select from 'react-select';
import {Tabs, Tab, Radio} from 'react-bootstrap';
import FSReactToastr from '../../../components/FSReactToastr';
import TopologyREST from '../../../rest/TopologyREST';
import CustomProcessorREST from '../../../rest/CustomProcessorREST';
import OutputSchema from '../../../components/OutputSchemaComponent';

export default class CustomNodeForm extends Component {
	static propTypes = {
		nodeData: PropTypes.object.isRequired,
		configData: PropTypes.object.isRequired,
		editMode: PropTypes.bool.isRequired,
		nodeType: PropTypes.string.isRequired,
		topologyId: PropTypes.string.isRequired,
		sourceNode: PropTypes.object.isRequired,
		targetNodes: PropTypes.array.isRequired,
		linkShuffleOptions: PropTypes.array.isRequired
	};

	constructor(props) {
		super(props);
		let {configData, editMode} = props;
		this.customConfig = JSON.parse(configData.config)
		let id = _.find(this.customConfig, {name: "name"}).defaultValue;
		let parallelism = _.find(this.customConfig, {name: "parallelism"}).defaultValue;
		this.fetchData(id);

		var obj = {
			editMode: editMode,
			showSchema: true,
			userInputs: [],
			showError: false,
			showErrorLabel: false
		};

		this.customConfig.map((o)=>{
			if(o.type === "boolean")
				obj[o.name] = o.defaultValue;
			else obj[o.name] = o.defaultValue ? o.defaultValue : '';
			if(o.isUserInput)
				obj.userInputs.push(o);
		});
		this.state = obj;
	}

	fetchData(id) {
		let {topologyId, nodeType, nodeData} = this.props;
		let promiseArr = [
			CustomProcessorREST.getProcessor(id),
			TopologyREST.getNode(topologyId, nodeType, nodeData.nodeId)
		];

		Promise.all(promiseArr)
			.then((results)=>{
				let {name, description, customProcessorImpl, imageFileName,	jarFileName,
					inputSchema, outputStreamToSchema, configFields} = results[0].entities[0];

				this.nodeData = results[1].entity;
				let properties = results[1].entity.config.properties;

				let stateObj = {
					parallelism: properties.parallelism,
					localJarPath: properties.localJarPath,
					name: name,
					description: description,
					customProcessorImpl: customProcessorImpl,
					imageFileName: imageFileName,
					jarFileName: jarFileName,
					inputSchema: inputSchema,
					outputStreamToSchema: outputStreamToSchema
				};

				this.state.userInputs.map((i)=>{
					if(i.type === "boolean")
						stateObj[i.name] = (properties[i.name]) === true ? true : false;
					else
						stateObj[i.name] = properties[i.name] ? properties[i.name] : '';
				});

				if(this.nodeData.outputStreams.length === 0)
					this.saveStreams(outputStreamToSchema);
				else stateObj.showSchema = true;

				this.setState(stateObj);
			})
			.catch((err)=>{
				console.error(err);
			})
	}

	saveStreams(outputStreamToSchema){
		let self = this;
		let {topologyId, nodeType} = this.props;
		let streamIds = _.keys(outputStreamToSchema),
			streamData = {},
			streams = [],
			promiseArr = [];

		streamIds.map((s)=>{
			streams.push({
				streamId: s,
				fields: outputStreamToSchema[s].fields
			});
		});

		streams.map((s)=>{
			promiseArr.push(TopologyREST.createNode(topologyId, 'streams', {body: JSON.stringify(s)}));
		});

		Promise.all(promiseArr)
			.then(results=>{
				self.nodeData.outputStreamIds = [];
				results.map(result=>{
						self.nodeData.outputStreamIds.push(result.entity.id);
					})
				TopologyREST.updateNode(topologyId, nodeType, self.nodeData.id, {body: JSON.stringify(this.nodeData)})
					.then((node)=>{
						self.nodeData = node.entity;
						self.setState({showSchema: true});
					})
			})
	}

	handleValueChange(fieldObj, e) {
		let obj = {
			showError: true,
			showErrorLabel: false
		};
		obj[e.target.name] = e.target.type === "number" && e.target.value !== '' ? Math.abs(e.target.value) : e.target.value;
		if(!fieldObj.isOptional) {
			if(e.target.value === '') fieldObj.isInvalid = true;
			else delete fieldObj.isInvalid;
		}
		this.setState(obj);
	}

	handleRadioBtn(e) {
		let obj = {};
		obj[e.target.dataset.name] = e.target.dataset.label === "true" ? true : false;
		this.setState(obj);
	}

	getData() {
		let obj = {},
			customConfig = this.customConfig;

		customConfig.map((o)=>{
			obj[o.name] = this.state[o.name];
		});
		return obj;
	}

	validateData(){
		let validDataFlag = true;

		this.state.userInputs.map((o)=>{
			if(!o.isOptional && this.state[o.name] === '') {
				validDataFlag = false;
				o.isInvalid = true;
			}
		});
		if(!validDataFlag)
			this.setState({showError: true, showErrorLabel: true});
		else this.setState({showErrorLabel: false});
		return validDataFlag;
	}

	handleSave(name){
		let {topologyId, nodeType} = this.props;
		let data = this.getData();
		let nodeId = this.nodeData.id;
		this.nodeData.config.properties = data;
		this.nodeData.name = name;

		return TopologyREST.updateNode(topologyId, nodeType, nodeId, {body: JSON.stringify(this.nodeData)})
	}

	render() {
		let {topologyId, editMode, nodeType, nodeData, targetNodes, linkShuffleOptions} = this.props;
		let {showSchema, showError, showErrorLabel} = this.state;
		return (
			<div>
				<Tabs id="customForm" defaultActiveKey={1} className="schema-tabs">
					<Tab eventKey={1} title="Configuration">
						<form className="form-horizontal">
							{
								this.state.userInputs.map((f, i)=>{
									return (
										<div className="form-group" key={i}>
										<label className="col-sm-3 control-label">{f.name}
											{f.isOptional ? null : '*'}
										</label>
										<div className="col-sm-6">
										{
										f.type === "boolean" ?
											[<Radio
												key="1"
												inline={true}
												data-label="true"
												data-name={f.name}
												onChange={this.handleRadioBtn.bind(this)}
												checked={this.state[f.name] ? true: false}
												disabled={!this.state.editMode}>true
											</Radio>,
											<Radio
												key="2"
												inline={true}
												data-label="false"
												data-name={f.name}
												onChange={this.handleRadioBtn.bind(this)}
												checked={this.state[f.name] ? false : true}
												disabled={!this.state.editMode}>false
											</Radio>]
						 				:
										<input
											name={f.name}
											value={this.state[f.name]}
											onChange={this.handleValueChange.bind(this, f)}
											type={f.type}
											className={!f.isOptional && showError && f.isInvalid ? "form-control invalidInput" : "form-control"}
									    	required={f.isOptional ? false : true}
									    	disabled={!this.state.editMode}
									    	min={f.type === "number" ? "0" : null}
									    	inputMode={f.type === "number" ? "numeric" : null}
										/>
										}
										</div>
										</div>
										);
								})
							}
						</form>
					</Tab>
					<Tab eventKey={2} title="Output Streams">
						{showSchema ?
							<OutputSchema
								topologyId={topologyId}
								editMode={editMode}
								nodeId={nodeData.nodeId}
								nodeType={nodeType}
								targetNodes={targetNodes}
								linkShuffleOptions={linkShuffleOptions}
								canAdd={false}
								canDelete={false}
							/>
						: null}
					</Tab>
				</Tabs>
			</div>
			)
	}
}