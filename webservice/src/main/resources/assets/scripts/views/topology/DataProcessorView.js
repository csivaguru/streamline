define(['require',
  'utils/LangSupport',
  'utils/Utils',
  'utils/Globals',
  'modules/Modal',
  'hbs!tmpl/topology/dataProcessorView'
], function(require, localization, Utils, Globals, Modal, tmpl) {
  'use strict';

  var DataProcessorView = Marionette.LayoutView.extend({

    template: tmpl,

    events: {
      'click #btnCancel': 'evClose',
      'click #btnAdd': 'evAdd'
    },

    regions: {
      formulaForm: '.formulaForm'
    },

    initialize: function(options) {
      _.extend(this, options);
      this.bindEvents();
    },

    bindEvents: function(){
      var self = this;
      this.listenTo(this.vent, 'change:Formula', function(data){
        self.generateForumla(data.models);
      });
    },

    generateForumla: function(models){
      var self = this;
      var msg = '';
      _.each(models, function(model){
        if(model.has('logical')){
          msg += '<span class="formulaLogical"> '+model.get('logical')+' </span>';
        } else if(!model.has('firstModel')){
          msg += '<span class="formulaError"> Missing Operator </span>';
        }
        
        if(model.has('field1')){
          msg += '<span class="formulaField">('+model.get('field1')+')</span>';
        } else {
          msg += '<span class="formulaError"> (Missing Field) </span>';
        }
        
        if(model.has('comp')){
          msg += '<span class="formulaComparison"> '+model.get('comp')+' </span>';
        } else {
          msg += '<span class="formulaError"> Missing Operator </span>';
        }

        if(model.has('field2')){
          msg += '<span class="formulaField">('+model.get('field2')+')</span>';
        } else {
          msg += '<span class="formulaError"> (Missing Field) </span>';
        }
      });
      this.$('#previewFormula').html(msg);
    },

    onRender:function(){
      var self = this;
      this.$('[data-rel="tooltip"]').tooltip();
      require(['views/topology/FormulaCompositeView'], function(FormulaCompositeView){
        self.view = new FormulaCompositeView({
          vent: self.vent
        });
        self.formulaForm.show(self.view);
      });
    },
    evAdd: function(e){
      this.vent.trigger('dataStream:SavedStep2', {});
      this.evClose();
    },
    evClose: function(e){
      this.trigger('closeModal');
    }

  });
  
  return DataProcessorView;
});