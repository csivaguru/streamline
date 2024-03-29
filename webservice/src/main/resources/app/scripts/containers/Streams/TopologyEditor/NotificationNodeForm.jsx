import React, {Component, PropTypes}from 'react';
import ReactDOM from 'react-dom';
import _ from 'lodash';
import {Radio} from 'react-bootstrap';
import TopologyREST from '../../../rest/TopologyREST';

export default class NotificationNodeForm extends Component {
	static propTypes = {
		nodeData: PropTypes.object.isRequired,
		configData: PropTypes.object.isRequired,
		editMode: PropTypes.bool.isRequired,
		nodeType: PropTypes.string.isRequired,
		topologyId: PropTypes.string.isRequired,
		sourceNodes: PropTypes.array.isRequired
	};

	constructor(props) {
		super(props);
		let {configData, editMode} = props;
		this.fetchData();
		if(typeof configData.config === 'string'){
			configData.config = JSON.parse(configData.config)
		}

		let obj = {
			configData: configData,
			configFields: {},
			editMode: editMode,
			showError: false,
			showErrorLabel: false,
			changedFields: []
		};
		configData.config.map(o => {
			if(o.type.search('array') !== -1){
				let s = {};
				o.defaultValue.map((d)=>{
					s[d.name] = d.defaultValue === null ? '' : d.defaultValue;
				})
				obj.configFields[o.name] = s;
			} else {
				obj.configFields[o.name] = o.defaultValue === null ? '' : o.defaultValue;
			}
		});

		this.rulesArr = [];
		this.windowsArr = [];
		this.state = obj;
		this.fetchRulesAndWindows();
	}

	fetchRulesAndWindows(){
		let {topologyId, sourceNodes} = this.props;
		let promiseArr = [];
		//Get all source nodes of notification and find rule processor
		//to update actions part if present
		sourceNodes.map((sourceNode)=>{
			promiseArr.push(TopologyREST.getNode(topologyId, 'processors', sourceNode.nodeId));
		})

		Promise.all(promiseArr)
			.then(results=>{
				let rulePromises = [];
				let windowPromises = [];
				results.map(result=>{
					let data = result.entity;
					if(data.type === 'RULE'){
						if(data.config.properties.rules){
							data.config.properties.rules.map(ruleId=>{
								rulePromises.push(TopologyREST.getNode(topologyId, 'rules', ruleId));
							})
						}
					} else if(data.type === 'WINDOW'){
						if(data.config.properties.rules){
							data.config.properties.rules.map(ruleId=>{
								windowPromises.push(TopologyREST.getNode(topologyId, 'windows', ruleId));
							})
						}
					}
				})
				Promise.all(rulePromises)
					.then(ruleResults=>{
						ruleResults.map((rule, i)=>{
							this.rulesArr.push(rule.entity);
						})
					})
				Promise.all(windowPromises)
					.then(windowResults=>{
						windowResults.map((window, i)=>{
							this.windowsArr.push(window.entity);
						})
					})
			})
	}

	fetchData(){
		let {topologyId, nodeType, nodeData} = this.props;
		let promiseArr = [
			TopologyREST.getNode(topologyId, nodeType, nodeData.nodeId),
		];

		Promise.all(promiseArr)
			.then((results)=>{
				//#1. get node result
				this.nodeData = results[0].entity;
				let configFields = this.syncData(results[0].entity.config.properties);

				this.setState({configFields: configFields});
			})
			.catch((err)=>{
				console.error(err);
			})
	}

	syncData(data){
		let keys = _.keys(data);
		let configFields = this.state.configFields;
		let configKeys = _.keys(configFields);
		configKeys.map((c)=>{
			if(typeof configFields[c] !== 'object'){
				configFields[c] = data[c] || configFields[c];
			} else {
				let internalKey = _.keys(configFields[c]);
				internalKey.map((i)=>{
					if(data[c]){
						configFields[c][i] = data[c][i] || configFields[c][i]
					}
				})
			}
		})
		return configFields;
	}

	handleValueChange(e) {
		let obj = this.state.configFields;
		let value = e.target.value;
		let parentObjKey = e.target.dataset.parentobjkey;
		let result = null;
		let changedFields = [...this.state.changedFields, e.target.name];
		if(value === ''){
			result = '';
		} else if(e.target.type === "number"){
			result = Math.abs(value);
		} else if(e.target.dataset.label === "true" || e.target.dataset.label === "true"){
			result = JSON.parse(e.target.dataset.label);
		} else {
			result = value;
		}
		if(parentObjKey){
			obj[parentObjKey][e.target.name] = result;
		} else {
			obj[e.target.name] = result
		}
		if(changedFields.indexOf(e.target.name) === -1)
			changedFields.push(e.target.name);
		this.setState({configFields: obj, showError: true, showErrorLabel: false, changedFields: changedFields});
	}

	validateData(){
		let {configFields, changedFields} = this.state;
		let validDataFlag = true;
		let configKeys = _.keys(configFields);
		configKeys.map((c)=>{
			if(typeof configFields[c] !== 'object'){
				if(configFields[c] === '') {
					if(changedFields.indexOf(c) === -1)
						changedFields.push(c);
					validDataFlag = false;
				}
			} else {
				let internalKey = _.keys(configFields[c]);
				internalKey.map((i)=>{
					if(configFields[c][i] === '') {
						if(changedFields.indexOf(i) === -1)
							changedFields.push(i);
						validDataFlag = false;
					}
				})
			}
		})
		if(!validDataFlag)
			this.setState({showError: true, showErrorLabel: true, changedFields: changedFields});
		else this.setState({showErrorLabel: false});
		return validDataFlag;
	}

	updateActions(obj, nodeName, name, type, promiseArr){
		let {topologyId} = this.props;
		if(obj && obj.actions){
			let updateFlag = false;
			obj.actions.map((a,i)=>{
				if(a.name === nodeName){
					a.notifierName = this.state.configFields.notifierName;
					a.outputFieldsAndDefaults = this.state.configFields.fieldValues;
					a.name = name;
					updateFlag = true;
				}
			});
			if(updateFlag){
				promiseArr.push(TopologyREST.updateNode(topologyId, type, obj.id, {body: JSON.stringify(obj)}))
			}
		}
	}

	handleSave(name){
		let {topologyId, nodeType} = this.props;
		let nodeId = this.nodeData.id;
		this.nodeData.config.properties = this.state.configFields;
		let nodeName = this.nodeData.name;
		this.nodeData.name = name;
		let promiseArr = [TopologyREST.updateNode(topologyId, nodeType, nodeId, {body: JSON.stringify(this.nodeData)})]
		if(this.rulesArr.length){
			this.rulesArr.map(rule=>{
				this.updateActions(rule, nodeName, name, 'rules', promiseArr);
			})
		}
		if(this.windowsArr.length){
			this.windowsArr.map(windowObj=>{
				this.updateActions(windowObj, nodeName, name, 'windows', promiseArr);
			})
		}
		return Promise.all(promiseArr);
	}

	render() {
		let {editMode, showError, changedFields, showErrorLabel, configFields} = this.state;
		return (
			<div>
				<form className="form-horizontal">
					<div className="row">
						<div className="col-sm-6">
							<div className="form-group">
								<label className="col-sm-4 control-label">Notifier Name</label>
								<div className="col-sm-8">
									<input
									name="notifierName"
									value={this.state.configFields.notifierName}
									type="text"
									className="form-control"
								    disabled={true}
									/>
								</div>
							</div>
						</div>
						<div className="col-sm-6">
							<div className="form-group">
								<label className="col-sm-4 control-label">Jar File Name</label>
								<div className="col-sm-8">
									<input
									name="jarFileName"
									value={this.state.configFields.jarFileName}
									type="text"
									className="form-control"
								    disabled={true}
									/>
								</div>
							</div>
						</div>
					</div>
					<div className="row">
						<div className="col-sm-6">
							<div className="form-group">
								<label className="col-sm-4 control-label">Class Name</label>
								<div className="col-sm-8">
									<input
									name="className"
									value={this.state.configFields.className}
									type="text"
									className="form-control"
								    disabled={true}
									/>
								</div>
							</div>
						</div>
						<div className="col-sm-6">
							<div className="form-group">
								<label className="col-sm-4 control-label">Hbase Config Key</label>
								<div className="col-sm-8">
									<input
									name="hbaseConfigKey"
									value={this.state.configFields.hbaseConfigKey}
									type="text"
									className="form-control"
								    disabled={true}
									/>
								</div>
							</div>
						</div>
					</div>
					<div className="row">
						<div className="col-sm-6">
							<div className="form-group">
								<label className="col-sm-4 control-label">Parallelism</label>
								<div className="col-sm-8">
									<input
									name="parallelism"
									value={this.state.configFields.parallelism}
									onChange={this.handleValueChange.bind(this)}
									type="number"
									className="form-control"
									required={true}
								    disabled={!this.state.editMode}
								    min="0"
									inputMode="numeric"
									/>
								</div>
							</div>
						</div>
					</div>
					<div className="row">
						<div className="col-sm-12">
							<fieldset className="fieldset-default">
								<legend>Properties</legend>
								<div className="row">
									<div className="col-sm-6">
										<div className="form-group">
											<label className="col-sm-4 control-label">Username *</label>
											<div className="col-sm-8">
												<input
												name="username"
												data-parentObjKey="properties"
												value={this.state.configFields.properties.username}
												onChange={this.handleValueChange.bind(this)}
												type="text"
												className={editMode && showError && changedFields.indexOf("username") !== -1 && configFields.properties.username === '' ? "form-control invalidInput" : "form-control"}
											    required={true}
								    			disabled={!this.state.editMode}
												/>
											</div>
										</div>
									</div>
									<div className="col-sm-6">
										<div className="form-group">
											<label className="col-sm-4 control-label">Password *</label>
											<div className="col-sm-8">
												<input
												name="password"
												data-parentObjKey="properties"
												value={this.state.configFields.properties.password}
												onChange={this.handleValueChange.bind(this)}
												type="password"
												className={editMode && showError && changedFields.indexOf("password") !== -1 && configFields.properties.password === '' ? "form-control invalidInput" : "form-control"}
											    required={true}
								    			disabled={!this.state.editMode}
												/>
											</div>
										</div>
									</div>
								</div>
								<div className="row">
									<div className="col-sm-6">
										<div className="form-group">
											<label className="col-sm-4 control-label">Host *</label>
											<div className="col-sm-8">
												<input
												name="host"
												data-parentObjKey="properties"
												value={this.state.configFields.properties.host}
												onChange={this.handleValueChange.bind(this)}
												type="text"
												className={editMode && showError && changedFields.indexOf("host") !== -1 && configFields.properties.host === '' ? "form-control invalidInput" : "form-control"}
											    required={true}
								    			disabled={!this.state.editMode}
												/>
											</div>
										</div>
									</div>
									<div className="col-sm-6">
										<div className="form-group">
											<label className="col-sm-4 control-label">Port *</label>
											<div className="col-sm-8">
												<input
												name="port"
												data-parentObjKey="properties"
												value={this.state.configFields.properties.port}
												onChange={this.handleValueChange.bind(this)}
												type="number"
												className={editMode && showError && changedFields.indexOf("port") !== -1 && configFields.properties.port === '' ? "form-control invalidInput" : "form-control"}
											    required={true}
								    			disabled={!this.state.editMode}
								    			min="0"
									    		inputMode="numeric"
												/>
											</div>
										</div>
									</div>
								</div>
								<div className="row">
									<div className="col-sm-6">
										<div className="form-group">
											<label className="col-sm-4 control-label">Start TLS</label>
											<div className="col-sm-8">
												<Radio 
													inline={true} 
													data-label="true" 
													name="starttls"
													data-parentObjKey="properties"
													onChange={this.handleValueChange.bind(this)} 
													checked={this.state.configFields.properties.starttls ? true: false}
													disabled={!this.state.editMode}>true
												</Radio>
												<Radio
													inline={true} 
													data-label="false" 
													name="starttls"
													data-parentObjKey="properties"
													onChange={this.handleValueChange.bind(this)} 
													checked={this.state.configFields.properties.starttls ? false : true}
													disabled={!this.state.editMode}>false
												</Radio>
											</div>
										</div>
									</div>
									<div className="col-sm-6">
										<div className="form-group">
											<label className="col-sm-4 control-label">Debug</label>
											<div className="col-sm-8">
												<Radio 
													inline={true} 
													data-label="true" 
													name="debug"
													data-parentObjKey="properties"
													onChange={this.handleValueChange.bind(this)} 
													checked={this.state.configFields.properties.debug ? true: false}
													disabled={!this.state.editMode}>true
												</Radio>
												<Radio
													inline={true} 
													data-label="false" 
													name="debug"
													data-parentObjKey="properties"
													onChange={this.handleValueChange.bind(this)} 
													checked={this.state.configFields.properties.debug ? false : true}
													disabled={!this.state.editMode}>false
												</Radio>
											</div>
										</div>
									</div>
								</div>
								<div className="row">
									<div className="col-sm-6">
										<div className="form-group">
											<label className="col-sm-4 control-label">SSL</label>
											<div className="col-sm-8">
												<Radio 
													inline={true} 
													data-label="true" 
													name="ssl"
													data-parentObjKey="properties"
													onChange={this.handleValueChange.bind(this)} 
													checked={this.state.configFields.properties.ssl ? true: false}
													disabled={!this.state.editMode}>true
												</Radio>
												<Radio
													inline={true} 
													data-label="false" 
													name="ssl"
													data-parentObjKey="properties"
													onChange={this.handleValueChange.bind(this)} 
													checked={this.state.configFields.properties.ssl ? false : true}
													disabled={!this.state.editMode}>false
												</Radio>
											</div>
										</div>
									</div>
									<div className="col-sm-6">
										<div className="form-group">
											<label className="col-sm-4 control-label">Auth</label>
											<div className="col-sm-8">
												<Radio 
													inline={true} 
													data-label="true" 
													name="auth"
													data-parentObjKey="properties"
													onChange={this.handleValueChange.bind(this)} 
													checked={this.state.configFields.properties.auth ? true: false}
													disabled={!this.state.editMode}>true
												</Radio>
												<Radio
													inline={true} 
													data-label="false" 
													name="auth"
													data-parentObjKey="properties"
													onChange={this.handleValueChange.bind(this)} 
													checked={this.state.configFields.properties.auth ? false : true}
													disabled={!this.state.editMode}>false
												</Radio>
											</div>
										</div>
									</div>
								</div>
								<div className="row">
									<div className="col-sm-6">
										<div className="form-group">
											<label className="col-sm-4 control-label">Protocol</label>
											<div className="col-sm-8">
												<input
												name="protocol"
												data-parentObjKey="properties"
												value={this.state.configFields.properties.protocol}
												onChange={this.handleValueChange.bind(this)}
												type="text"
												className="form-control"
												required={true}
											    disabled={!this.state.editMode}
												/>
											</div>
										</div>
									</div>
								</div>
							</fieldset>
						</div>
					</div>
					<div className="row">
						<div className="col-sm-12">
							<fieldset className="fieldset-default">
								<legend>Field Values</legend>
								<div className="row">
									<div className="col-sm-6">
										<div className="form-group">
											<label className="col-sm-4 control-label">From *</label>
											<div className="col-sm-8">
												<input
												name="from"
												data-parentObjKey="fieldValues"
												value={this.state.configFields.fieldValues.from}
												onChange={this.handleValueChange.bind(this)}
												type="text"
												className={editMode && showError && changedFields.indexOf("from") !== -1 && configFields.fieldValues.from === '' ? "form-control invalidInput" : "form-control"}
											    required={true}
								    			disabled={!this.state.editMode}
												/>
											</div>
										</div>
									</div>
									<div className="col-sm-6">
										<div className="form-group">
											<label className="col-sm-4 control-label">To *</label>
											<div className="col-sm-8">
												<input
												name="to"
												data-parentObjKey="fieldValues"
												value={this.state.configFields.fieldValues.to}
												onChange={this.handleValueChange.bind(this)}
												type="text"
												className={editMode && showError && changedFields.indexOf("to") !== -1 && configFields.fieldValues.to === '' ? "form-control invalidInput" : "form-control"}
											    required={true}
								    			disabled={!this.state.editMode}
												/>
											</div>
										</div>
									</div>
								</div>
								<div className="row">
									<div className="col-sm-6">
										<div className="form-group">
											<label className="col-sm-4 control-label">Subject *</label>
											<div className="col-sm-8">
												<input
												name="subject"
												data-parentObjKey="fieldValues"
												value={this.state.configFields.fieldValues.subject}
												onChange={this.handleValueChange.bind(this)}
												type="text"
												className={editMode && showError && changedFields.indexOf("subject") !== -1 && configFields.fieldValues.subject === '' ? "form-control invalidInput" : "form-control"}
											    required={true}
								    			disabled={!this.state.editMode}
												/>
											</div>
										</div>
									</div>
									<div className="col-sm-6">
										<div className="form-group">
											<label className="col-sm-4 control-label">Content type</label>
											<div className="col-sm-8">
												<input
												name="contentType"
												data-parentObjKey="fieldValues"
												value={this.state.configFields.fieldValues.contentType}
												onChange={this.handleValueChange.bind(this)}
												type="text"
												className="form-control"
								    			disabled={!this.state.editMode}
												/>
											</div>
										</div>
									</div>
								</div>
								<div className="row">
									<div className="col-sm-6">
										<div className="form-group">
											<label className="col-sm-4 control-label">Body *</label>
											<div className="col-sm-8">
												<textarea
													name="body"
													data-parentObjKey="fieldValues"
													value={this.state.configFields.fieldValues.body}
													onChange={this.handleValueChange.bind(this)}
													className={editMode && showError && changedFields.indexOf("body") !== -1 && configFields.fieldValues.body === '' ? "form-control invalidInput" : "form-control"}
													required={true}
								    				disabled={!this.state.editMode}
												/>
											</div>
										</div>
									</div>
								</div>
							</fieldset>
						</div>
					</div>
				</form>
			</div>
		)
	}
}