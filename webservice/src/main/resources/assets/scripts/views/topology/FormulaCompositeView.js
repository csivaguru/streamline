define(['require',
	'utils/Globals',
	'hbs!tmpl/topology/formulaCompositeView',
	'views/topology/FormulaItemView'
], function(require, Globals, tmpl, FormulaItemView){
	'use strict';
	var FormulaCompositeView = Marionette.CompositeView.extend({
		template: tmpl,
		templateHelpers: function(){
            var self = this;
            this.comparisonOpArr = [];
            this.logicalOpArr = [];
            _.each(Globals.Functions.Comparison, function(obj){
                self.comparisonOpArr.push({
                    val:obj.valStr,
                    lbl: obj.value
                });
            });
            _.each(Globals.Functions.Logical, function(obj){
                self.logicalOpArr.push({
                    val:obj.valStr,
                    lbl: obj.value
                });
            });
            return {
                comparisonArr : this.comparisonOpArr,
                logicalArr : this.logicalOpArr
            };
        },

        childView: FormulaItemView,

        childViewContainer: "div[data-id='addRowDiv']",

        childViewOptions: function() {
            return {
                collection: this.collection,
                comparisonArr: this.comparisonOpArr,
                logicalArr: this.logicalOpArr,
                id: this.rowId++,
                vent: this.vent
            };
        },

        events: {
        	'click #addNewRule': 'evAddRow',
        	'change .ruleRow': 'evChange'
        },

        initialize: function(options){
        	_.extend(this, options);
            this.firstFormulaModel = new Backbone.Model({firstModel: true});
        	this.rowId = 2; //1 is for first row already rendered
        	this.collection = new Backbone.Collection();
        },

        generateForumla: function(collection){
          console.log(dataCollection);
        },

        onRender: function(){},
        evAddRow: function(){
        	this.collection.add(new Backbone.Model({id: this.rowId}));
        },
        evChange: function(e){
            var currentTarget = $(e.currentTarget);
            if(currentTarget.data().row == 1){
                this.firstFormulaModel.set(currentTarget.data().rowtype, currentTarget.val());
            } else {
                this.collection.get(currentTarget.data().row).set(currentTarget.data().rowtype, currentTarget.val());
            }
            var tempArr = [];
            tempArr.push(this.firstFormulaModel);
            if(this.collection.length){
                Array.prototype.push.apply(tempArr, this.collection.models);
            }
            this.vent.trigger('change:Formula', {
                models: tempArr
            });
        },
	});
	return FormulaCompositeView;
});