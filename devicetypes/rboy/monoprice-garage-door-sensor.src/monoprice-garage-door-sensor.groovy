/*
 * -----------------------
 * --- DEVICE HANDLER ----
 * -----------------------
 *
 * STOP:  Do NOT PUBLISH the code to GitHub, it is a VIOLATION of the license terms.
 * You are NOT allowed share, distribute, reuse or publicly host (e.g. GITHUB) the code. Refer to the license details on our website.
 *
 */

/* **DISCLAIMER**
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * Without limitation of the foregoing, Contributors/Regents expressly does not warrant that:
 * 1. the software will meet your requirements or expectations;
 * 2. the software or the software content will be free of bugs, errors, viruses or other defects;
 * 3. any results, output, or data provided through or generated by the software will be accurate, up-to-date, complete or reliable;
 * 4. the software will be compatible with third party software;
 * 5. any errors in the software will be corrected.
 * The user assumes all responsibility for selecting the software and for the results obtained from the use of the software. The user shall bear the entire risk as to the quality and the performance of the software.
 */ 
 
def clientVersion() {
    return "01.01.00"
}

/**
 * Monoprice Garage Door Sensor
 * 
 * Copyright RBoy Apps, redistribution or reuse of code is not allowed without permission
 * Change log:
 * 2018-10-10 - (v01.01.00) Process BasicSet notification to improve resilience to mesh issues
 * 2018-10-10 - (v01.00.03) Fix typo for icon name
 * 2018-8-5 - (v01.00.02) Added health check capability and basic support for new ST app
 * 2018-03-23 - (v01.00.01) Get rid of excess covering closed notitications
 * 2018-02-22 - (v01.00.00) Initial release
 *
 *  Copyright 2014 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */

preferences {
    input title: "", description: "Monoprice Garage Door Sensor Device Handler v${clientVersion()}", displayDuringSetup: false, type: "paragraph", element: "paragraph"
	input title: "", description: "This device can use the internal and external wired sensors simultaneously. You can choose to disable either sensor", displayDuringSetup: false, type: "paragraph", element: "paragraph"
    input "disableInternalSensor", "bool", title: "Disable internal sensor", displayDuringSetup: false, required: false
    input "disableExternalSensor", "bool", title: "Disable external sensor", displayDuringSetup: false, required: false
	input title: "", description: "By default the device considers external sensors to be Normally Closed (N/C), enable the option below if your external sensor is Normally Open (N/O)", displayDuringSetup: false, type: "paragraph", element: "paragraph"
    input "externalSensorNO", "bool", title: "External wired sensor is N/O", displayDuringSetup: false, required: false
}

metadata {
	definition (name: "Monoprice Garage Door Sensor", namespace: "rboy", author: "RBoy Apps", ocfDeviceType: "x.com.st.d.sensor.contact", mnmn: "SmartThings", vid:"generic-contact") {
		capability "Configuration"
		capability "Contact Sensor"
		capability "Sensor"
		capability "Battery"
        capability "Tamper Alert"
        capability "Health Check"
        
        attribute "codeVersion", "string"
        attribute "dhName", "string"

        fingerprint deviceId:"0x0701", inClusters:"0x71,0x85,0x80,0x72,0x30,0x86,0x84", manufacturer: "Monoprice", model: "11987"
        
        // New fingerprint format (MSR ==> mfr-prod-model)
        fingerprint type:"2001", mfr: "0109", prod: "200A", model: "0A02", deviceJoinName:"Monoprice Garage Door Sensor (11987)" // cc:"71,85,80,72,30,86,84"
	}

	// UI tile definitions
    tiles(scale: 2) {
        multiAttributeTile(name:"summary", type: "generic", width: 6, height: 4, canChangeIcon: true) {
            tileAttribute ("device.contact", key: "PRIMARY_CONTROL") {
                attributeState "open", label: '${name}', icon: "st.contact.contact.open", backgroundColor: "#e86d13"
                attributeState "closed", label: '${name}', icon: "st.contact.contact.closed", backgroundColor: "#00A0DC"
            }
            tileAttribute ("device.battery", key: "SECONDARY_CONTROL") {
				attributeState "battery", label:'${currentValue}%'
            }
        }
		valueTile("battery", "device.battery", width: 2, height: 2, inactiveLabel: false) {
            state "battery", label:'${currentValue}%', unit: "", icon: "http://smartthings.rboyapps.com/images/battery.png",
                backgroundColors:[
                    [value: 15, color: "#ff0000"],
                    [value: 30, color: "#fd4e3a"],
                    [value: 50, color: "#fda63a"],
                    [value: 60, color: "#fdeb3a"],
                    [value: 75, color: "#d4fd3a"],
                    [value: 90, color: "#7cfd3a"],
                    [value: 99, color: "#55fd3a"]
                ]
        }
		standardTile("contact", "device.contact", width: 4, height: 4, inactiveLabel: false) {
			state "open", label: '${name}', icon: "st.contact.contact.open", backgroundColor: "#e86d13"
			state "closed", label: '${name}', icon: "st.contact.contact.closed", backgroundColor: "#00A0DC"
		}
		standardTile("tamper", "device.tamper", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "clear", label:'Request update', action:"configure", backgroundColor:"#FFFFFF", defaultState: true
			state "detected", label:'TAMPER', backgroundColor:"#e86d13"
		}

		main "contact"
        details(["contact", "battery", "tamper"])
	}
}

def parse(String description) {
    sendEvent([name: "codeVersion", value: clientVersion()]) // Save client version for parent app
    sendEvent([name: "dhName", value: "Monoprice Garage Door Sensor Device Handler"]) // Save DH Name for parent app

	def result = null
	if (description.startsWith("Err 106")) {
		if (state.security) {
			log.debug description
		} else {
			result = createEvent(
				descriptionText: "This sensor failed to complete the network security key exchange. If you are unable to control it via SmartThings, you must remove it from your network and add it again.",
				eventType: "ALERT",
				name: "secureInclusion",
				value: "failed",
				isStateChange: true,
			)
		}
	} else if (description != "updated") {
		def cmd = zwave.parse(description, [0x20: 1, 0x25: 1, 0x30: 2, 0x80: 1, 0x84: 2, 0x71: 3, 0x9C: 1, 0x70: 1])
		if (cmd) {
			result = zwaveEvent(cmd)
		}
	}
    log.debug "Parse returned ${result}"
	return result
}

def installed() {
	log.trace "Installed called settings: $settings"
	sendEvent(name: "checkInterval", value: 2 * 4 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	// this is the nuclear option because the device often goes to sleep before we can poll it
	response(configure())
}

def updated() {
	log.trace "Update called settings: $settings"
	sendEvent(name: "checkInterval", value: 2 * 4 * 60 * 60 + 2 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	// this is the nuclear option because the device often goes to sleep before we can poll it
	response(configure())
}

def configure() {
	log.trace "Configure called: $settings"
    
    state.setConfig = true // Set the config at next wakeup
    state.forceRefresh = true // Reset it to force a battery update on the next wake up
    
    // Configure is called at inclusion so we have a SMALL window to execute some commands here, won't make any differnce when called manually
    def cmds = []

    cmds += commands([
        zwave.batteryV1.batteryGet(),
        zwave.manufacturerSpecificV2.manufacturerSpecificGet()
    ], 500) // Small delay works since typically at inclusion the sensor is near the hub for the NIF command so there's no routing involved here

    // Don't do a no more wakeup here as the hub ends up queueing it becase it's a passive device and it interfers with the wakup command due to a delayed retry from the hub, the device will timeout by itself in 10 seconds
    //cmds << "delay 3000"
    //cmds << command(zwave.wakeUpV1.wakeUpNoMoreInformation()) // We're done here
    
    return cmds
}


def sensorValueEvent(value) {
	if (value) {
		createEvent(name: "contact", value: "open", descriptionText: "$device.displayName is open")
	} else {
		createEvent(name: "contact", value: "closed", descriptionText: "$device.displayName is closed")
	}
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd)
{
	log.trace "BasicReport: $cmd"
	//sensorValueEvent(cmd.value)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd)
{
	log.trace "BasicSet: $cmd"

    def result = []
    if (!disableInternalSensor) {
        result << sensorValueEvent(cmd.value)
    } else {
        log.debug "Internal sensor disabled, ignoring ${cmd.value ? "closed" : "open"} notification"
    }
    
    result
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd)
{
	log.trace "SwitchBinaryReport: $cmd"
	//sensorValueEvent(cmd.value)
}

def zwaveEvent(physicalgraph.zwave.commands.sensorbinaryv2.SensorBinaryReport cmd)
{
	log.trace "SensorBinaryReport: $cmd"
	//sensorValueEvent(cmd.sensorValue)
}

def zwaveEvent(physicalgraph.zwave.commands.sensoralarmv1.SensorAlarmReport cmd)
{
	log.trace "SensorAlarmReport: $cmd"
	//sensorValueEvent(cmd.sensorState)
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd)
{
	log.trace "NotificationReport: $cmd"
	def result = []
	if (cmd.notificationType == 0x07) {
        switch(cmd.event) {
            case 3:
            	result << createEvent(name: "tamper", value: "detected", descriptionText: "$device.displayName covering was removed", isStateChange: true)
            	state.forceRefresh = true // When the device is opened physically force a sensor refresh
            	break
            
            case 0:
            	result << createEvent(name: "tamper", value: "clear", descriptionText: "$device.displayName covering was closed", isStateChange: true)
            	break
            
            case 2: // Internal sensor
            	if (!disableInternalSensor) {
                    result << sensorValueEvent(cmd.v1AlarmLevel)
                } else {
                    log.debug "Internal sensor disabled, ignoring ${cmd.v1AlarmLevel ? "closed" : "open"} notification"
                }
            	break
            
            case 254: // External sensor
            	if (!disableExternalSensor) {
                    result << sensorValueEvent(externalSensorNO ? !cmd.v1AlarmLevel : cmd.v1AlarmLevel)
                } else {
                    log.debug "External sensor disabled, ignoring ${(externalSensorNO ? !cmd.v1AlarmLevel : cmd.v1AlarmLevel) ? "closed" : "open"} notification"
                }
	            break
            
            default:
            	log.warn "Unknown event type: $cmd.event"
            	break
        }
	} else if (cmd.notificationType) {
		def text = "Notification $cmd.notificationType: event ${([cmd.event] + cmd.eventParameter).join(", ")}"
		result << createEvent(name: "notification$cmd.notificationType", value: "$cmd.event", descriptionText: text, displayed: false)
	} else {
		def value = cmd.v1AlarmLevel == 255 ? "active" : cmd.v1AlarmLevel ?: "inactive"
		result << createEvent(name: "alarm $cmd.v1AlarmType", value: value, displayed: false)
	}
	result
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd)
{
	log.debug "Device woke up"
    
	def event = createEvent(descriptionText: "${device.displayName} woke up", isStateChange: false)
    sendEvent(name: "tamper", value: "clear", descriptionText: "$device.displayName covering was closed") // When the cover is closed it issues a wake up command, best way to detect tamper off for now (reset on wake up)

	def cmds = []

    if (!state.MSR) {
        log.debug "Getting device MSR"
		cmds << zwave.manufacturerSpecificV2.manufacturerSpecificGet()
	} else {
        log.debug "Saved MSR: $state.MSR"
    }

    if (state.setConfig) { // Set configuration
        state.setConfig = false // We're done, dont' update again unless requested
    }

	/*if (state.forceRefresh || (device.currentValue("contact") == null)) { // Incase our initial request didn't make it or we are asking for a refresh
        log.debug "Getting sensor state"
		cmds << zwave.basicV1.basicGet()
	}*/ // BasicGet reports the last sensor state and not the current one, during tamper it doesn't report an information and after a tamper it reports the last state so no use calling this

    // Get battery updates every 24 hours to save battery
	if (state.forceRefresh || !state.lastbat || (now() - state.lastbat > 24*60*60*1000)) {
        log.debug "Getting battery level"
        cmds << zwave.batteryV1.batteryGet()
    }
    cmds << zwave.wakeUpV2.wakeUpNoMoreInformation()
    
    state.forceRefresh = false // We're done until the next request

	[event, getResponses(cmds)]
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	log.trace "BatteryReport: $cmd"
    
	def map = [ name: "battery", unit: "%" ]
	if (cmd.batteryLevel == 0xFF) {
        map.value = 1
		map.descriptionText = "${device.displayName} has a low battery"
		map.isStateChange = true
	} else {
		map.value = cmd.batteryLevel
	}
	state.lastbat = now()
	[createEvent(map)]
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpIntervalReport cmd) {
    log.trace "WakeUpIntervalReport $cmd"
}

// NOTE: Bug with firmware it's always 0 (wake up when sensor opens)
// WakeUpIntervalCapabilitiesReport(defaultWakeUpIntervalSeconds: 0, maximumWakeUpIntervalSeconds: 0, minimumWakeUpIntervalSeconds: 0, wakeUpIntervalStepSeconds: 0)
def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpIntervalCapabilitiesReport cmd) {
    log.trace "WakeUpIntervalCapabilitiesReport $cmd"
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	def result = []

	def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
	log.debug "msr: $msr"
	updateDataValue("MSR", msr)

	result << createEvent(descriptionText: "$device.displayName MSR: $msr", isStateChange: false)

    if (!device.currentState("battery")) {
        result << getResponse(zwave.batteryV1.batteryGet())
    }

	result
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
	log.trace "ConfigurationReport $cmd"
    def result = []
    def msg = null
    switch (cmd.parameterNumber) {
        default:
            log.warn "Unknown parameter"
            break
    }
    
    log.info msg
    result
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	def encapsulatedCommand = cmd.encapsulatedCommand([0x20: 1, 0x25: 1, 0x30: 2, 0x80: 1, 0x84: 2, 0x71: 3, 0x9C: 1, 0x70: 1])
	// log.debug "encapsulated: $encapsulatedCommand"
	if (encapsulatedCommand) {
		state.security = true
		zwaveEvent(encapsulatedCommand)
	}
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.warn "Unhandled command: $cmd"
	createEvent(descriptionText: "$device.displayName: $cmd", displayed: false)
}

private getResponses(commands, delay=1200) {
    if (state.security) {
        response(delayBetween(commands.collect{ zwave.securityV1.securityMessageEncapsulation().encapsulate(it).format() }, delay))
    } else {
        response(delayBetween(commands.collect{ it.format() }, delay))
    }
}

private getResponse(command) {
    if (state.security) {
        response(zwave.securityV1.securityMessageEncapsulation().encapsulate(command).format())
    } else {
        response(command.format())
    }
}

private command(physicalgraph.zwave.Command cmd) {
	if (state.security) {
		zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	} else {
		cmd.format()
	}
}

private commands(commands, delay=200) {
	delayBetween(commands.collect{ command(it) }, delay)
}

// THIS IS THE END OF THE FILE