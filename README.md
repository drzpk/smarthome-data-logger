# Smart Home Data Logger

This program collects data and sends them to the Smart Home server(s).

## Modules

Data logger consists of modules that are responsible for communication with different server-side applications. A module
is only enabled when a specific property is defined in the configuration file.

Configuration is defined in the file `config.properties` that must exist in the same directory as the JAR

Common (module-independent) configuration properties are defined below:

```properties
# Directory where logs will be saved
log_directory=/tmp/data-logger-logs
```

### PV-Stats module

The PV-Stats module is responsible for collecting data
from [solar inverters](https://en.wikipedia.org/wiki/Solar_inverter)
and sending it to the [PV-Stats server](https://gitlab.com/smart-home-dr/pv-stats).

To enable this module, define the following properties in the configuration file:

```properties
# Address of the PV-Stats instance
pvstats.url=https://pv-ctl.domain.com
# Connection timeout to pv-stats server (seconds)
pvstats.timeout=3
```

#### Interter configuration

Properties described in this section are required for each inverter type. `Example` is the name of the source, that is
arbitrary but must be unique.

```properties
# Interver type
pvstats.source.example.type=SOFAR
# Data source credentials (defined in pv-stats)
pvstats.source.example.user=viewer_sofar__example
pvstats.source.example.password=3ddAsoQDjkf3LjuRsaOSLC
# Data source connection timeout (seconds)
pvstats.source.example.timeout=3
# Interval between current/"light" stats (such as power, voltage, in seconds)
pvstats.source.example.metrics_interval=15
# Interval between "heavy" measurements (containing all historical data from certain period, in seconds)
# (only supported by SMA inverters)
pvstats.source.example.measurement_interval=60
```

#### SMA inverter configuration

```properties
pvstats.source.example.type=SMA
# SNA inverter url
pvstats.source.example.url=https://192.168.1.100:80
# Common configuration goes here...
```

#### Sofar inverter configuration (Wi-Fi)

This configuration allows connecting with an inverter using a wierless dongle.

```properties
pvstats.source.example.type=SOFAR_WIFI
# Sofar inverter URL (port should be 8899)
pvstats.source.example.url=192.168.1.100:8899
# Inverter WiFi dongle's serial number (visible on the dongle itself or its box)
pvstats.source.example.sn=2952164280
# Common configuration goes here...
```

#### Sofar inverter configuration (Modbus)

This configuration allows connecting with an inverter using a Modbus protocol over RS485. A RS485 communication device
is required to be installed in the client device.

```properties
pvstats.source.example.type=SOFAR_MODBUS
# RS485 device path (COM1, COM2 etc. on Windows)
pvstats.source.example.devpath=/dev/ttyUSB0
# Inverter slave ID (usually 1)
pvstats.source.example.slaveId=1
# Common configuration goes here...
```

If using this configuration on a linux machine, make sure that the user you start the data-logger with has required
permissions to the serial device, otherwise you will end up with *permission denied* exceptions. Usually addding a user
to the `dialout` group should solve this issue:

```shell
sudo usermod -a -G dialout <user running the data-logger>
```

### Sensors module

Sensors module collects data from different types of sensors and stores in
the [server](https://gitlab.com/smart-home-dr/sensors).

To enable this module, define the following configuration properties:
```properties
# URL of the Sensors server
sensors.serverUrl=http://localhost:8080
# Credentials of this logger. There can be multiple loggers, and each of them must use unique credentials.
sensors.loggerId=1
sensors.loggerSecret=secret
```

In order for a device to be recognized by the data logger, it must be first defined in the sensors server.

#### Bluetooth thermometers

The sensors module collects data from Xiaomi Mijia LYWSD03MMC devices. They must be running
[this](https://github.com/pvvx/ATC_MiThermometer) firmware and their beacon format must be set to *custom*.

Bluetooth may be blocked by default on some machines, and it must be enabled first:
```shell
sudo rfkill unblock bluetooth
```
