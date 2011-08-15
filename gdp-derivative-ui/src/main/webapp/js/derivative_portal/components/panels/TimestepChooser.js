Ext.ns("GDP");

GDP.TimestepChooser = Ext.extend(Ext.form.FormPanel, {
    maxNumberOfTimesteps : 301,
    LOW_THUMB : 0,
    MIDDLE_THUMB : 1,
    HIGH_THUMB : 2,
    timestepStore : undefined,
    layerController : undefined,
    timestepComponent : undefined,
    timestepSlider : undefined,
    timestepAnimator : undefined,
    updateAvailableTimesteps : function(record) {
        if (!record) return;
		
        var loaded = this.layerController.loadDimensionStore(record, this.timestepStore, 'time', this.maxNumberOfTimesteps);
		
        if (loaded) {
            this.timestepAnimator.setMinIndex(0);
            this.timestepSlider.setMinValue(0);

            this.timestepAnimator.setMaxIndex(loaded.loadedData.length - 1);
            this.timestepSlider.setMaxValue(loaded.loadedData.length - 1);

            this.timestepComponent.setValue(loaded.currentExtent);
            this.setThumbValue(this.MIDDLE_THUMB, loaded.currentExtent);
        }
    },
    constructor : function(config) {
        config = config || {};
		
        this.layerController = config.layerController || new GDP.LayerController();
		
        var stepStore = new Ext.data.ArrayStore({
            storeId : 'timestepStore',
            idIndex: 0,
            fields: ['time']
        });
        this.timestepStore = stepStore;
		
        this.timestepComponent = new Ext.form.TextField({
            editable : false
        });
		
        this.timestepSlider = new Ext.slider.MultiSlider({
            hideLabel : true,
            flex : 1,
            minValue : 0,
            maxValue : 100,
            constrainThumbs : false,
            values : [0, 50, 100],
            plugins : new Ext.slider.Tip({
                getText: function(thumb) {
                    return stepStore.getAt(thumb.value).get('time');
                }
            })
        });
		
        this.timestepAnimator = new GDP.Animator();
		
		
        config = Ext.apply({
            items : [
            new Ext.form.CompositeField({
                hideLabel : true,
                items : [
                this.timestepSlider,
                this.timestepComponent,
                new Ext.Button({
                    text : 'Play',
                    handler : function() {
                        this.timestepAnimator.startAnimation(this.getThumbValue(this.MIDDLE_THUMB));
                    },
                    scope : this
                }),
                new Ext.Button({
                    text : 'Stop',
                    handler : function() {
                        this.timestepAnimator.stopAnimation();
                    },
                    scope : this
                })
                ]
            })
            ]
        }, config);
		
        GDP.TimestepChooser.superclass.constructor.call(this, config);
		
        LOG.debug('TimestepChooser:constructor: Registering Listeners.');
        this.layerController.on('changelayer', function() {
            LOG.debug('TimestepChooser: Observed "changelayer"');
            this.updateAvailableTimesteps(this.layerController.getLayer());
        }, this); 
		
        this.layerController.on('changedimension', function() {
            LOG.debug('TimestepChooser: Observed "changedimension"');
            var currentTimestep = this.layerController.getDimension('time');
            this.timestepComponent.setValue(currentTimestep);
            this.setThumbValue(this.MIDDLE_THUMB, currentTimestep);
        }, this);
                
        this.timestepSlider.on('changecomplete', function(slider, newValue, thumb) {
            LOG.debug('TimestepChooser: Observed "changecomplete"');
            this.timestepAnimator.setMinIndex(this.getThumbValue(this.LOW_THUMB));
            this.timestepAnimator.setMaxIndex(this.getThumbValue(this.HIGH_THUMB));
            var midVal = this.getThumbValue(this.MIDDLE_THUMB);
            if (midVal === newValue) {
                this.layerController.requestDimension('time', this.timestepStore.getAt(midVal).get('time'));
            }
        }, this);
        this.timestepAnimator.on('timedchange', function(index) {
            LOG.debug('TimestepChooser: Observed "timedchange"');
            this.layerController.requestDimension('time', this.timestepStore.getAt(index).get('time'));
        }, this);
                
    },
    getSortedThumbs : function() {
        var thumbArray = this.timestepSlider.thumbs.slice(0);
        thumbArray.sort(function(a,b) {
            return a.value - b.value
        });
        return thumbArray;
    },
    getThumbValue : function(index) {
        var thumbArray = this.getSortedThumbs();
        return thumbArray[index].value;
    },
    setThumbValue : function(index, time){
        var thumbArray = this.getSortedThumbs();
        this.timestepSlider.setValue(thumbArray[index].index, this.timestepStore.indexOfId(time));
    }
});