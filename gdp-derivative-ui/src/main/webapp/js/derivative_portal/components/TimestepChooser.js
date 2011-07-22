Ext.ns("GDP");

GDP.TimestepChooser = Ext.extend(Ext.form.FormPanel, {
	maxNumberOfTimesteps : 5,
	timestepStore : undefined,
	layerController : undefined,
	timestepComponent : undefined,
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
		this.timestepAnimator.setMaxIndex(timesToLoad.length - 1);
		this.timestepComponent.setValue(currentTimestep);
	},
	constructor : function(config) {
		config = config || {};
		
		this.layerController = config.layerController || new GDP.LayerController();
		
		this.layerController.on('changelayer', function() {
			this.updateAvailableTimesteps(this.layerController.getLayer());
		}, this); 
		
		this.layerController.on('changedimension', function() {
			this.timestepComponent.setValue(this.layerController.getTimestep());
		}, this);
		
		this.timestepStore = new Ext.data.ArrayStore({
			storeId : 'timestepStore',
			idIndex: 0,
			fields: ['time']
		});
		
		this.timestepComponent = new Ext.form.ComboBox({
			mode : 'local',
			triggerAction: 'all',
			flex : 1,
			anchor : '100%',
			store : this.timestepStore,
			displayField : 'time'
		});
		
		this.timestepComponent.on('select', function(combo, record, index) {
			var timeStr = record.get('time');
			this.layerController.requestTimestep(timeStr);
		}, this);
		
		this.timestepAnimator = new GDP.Animator();
		
		this.timestepAnimator.on('timedchange', function(index) {
			this.layerController.requestTimestep(this.timestepStore.getAt(index).get('time'));
		}, this);
		
		config = Ext.apply({
			items : [
				new Ext.form.CompositeField({
					fieldLabel : 'Timestep',
					items : [
						this.timestepComponent,
			new Ext.Button({
				text : 'Play',
				handler : function() {
					this.timestepAnimator.startAnimation();
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
		
	}
});