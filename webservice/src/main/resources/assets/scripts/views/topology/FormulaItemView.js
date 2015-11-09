define(['require',
    'utils/Globals',
    'hbs!tmpl/topology/formulaItemView'
],function(require, Globals, tmpl) {
    'use strict';

    return Marionette.ItemView.extend(
    {
        template: tmpl,
        templateHelpers: function(){
            return {
                comparisonArr : this.comparisonArr,
                logicalArr : this.logicalArr,
                id: this.id
            };
        },
        events: {
            'click .btnDelete': 'evDelete',
        },

        initialize: function(options) {
            _.extend(this, options);
        },

        onRender: function() {

        },

        evDelete: function() {
            this.collection.remove(this.model.id);
        }
    });
});
