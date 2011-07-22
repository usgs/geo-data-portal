Ext.ns("GDP");

GDP.TimestepChooser = Ext.extend(Ext.form.FormPanel, {
	maxNumberOfTimesteps : 101,
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
		this.timestepStore.removeAll();
		var times = record.get('dimensions').time;
		var currentTimestep = times['default'];
		var timesToLoad = [];
		Ext.each(times.values, function(item, index, allItems){
			if (index > this.maxNumberOfTimesteps) {
				return false;
			} else {
				timesToLoad.push([item.trim()]);
			}
			return true;
		}, this);
		
		this.timestepStore.loadData(timesToLoad);
		this.timestepAnimator.setMinIndex(0);
		this.timestepSlider.setMinValue(0);
		this.timestepAnimator.setMaxIndex(timesToLoad.length - 1);
		this.timestepSlider.setMaxValue(timesToLoad.length - 1);
		this.timestepComponent.setValue(currentTimestep);
		this.setThumbValue(this.MIDDLE_THUMB, currentTimestep);
	},
	constructor : function(config) {
		config = config || {};
		
		this.layerController = config.layerController || new GDP.LayerController();
		
		this.layerController.on('changelayer', function() {
			this.updateAvailableTimesteps(this.layerController.getLayer());
		}, this); 
		
		this.layerController.on('changedimension', function() {
			var currentTimestep = this.layerController.getTimestep();
			this.timestepComponent.setValue(currentTimestep);
			this.setThumbValue(this.MIDDLE_THUMB, currentTimestep);
		}, this);
		
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
		
		this.timestepSlider.on('changecomplete', function(slider, newValue, thumb) {
			this.timestepAnimator.setMinIndex(this.getThumbValue(this.LOW_THUMB));
			this.timestepAnimator.setMaxIndex(this.getThumbValue(this.HIGH_THUMB));
			this.layerController.requestTimestep(this.timestepStore.getAt(this.getThumbValue(this.MIDDLE_THUMB)).get('time'));
		}, this);
		
		this.timestepAnimator.on('timedchange', function(index) {
			this.layerController.requestTimestep(this.timestepStore.getAt(index).get('time'));
		}, this);
		
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
		
	},
	getSortedThumbs : function() {
		var thumbArray = this.timestepSlider.thumbs.slice(0);
		thumbArray.sort(function(a,b) {return a.value - b.value});
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