**BATTERY READER** </br>
Battery Reader allows to retrieve the information that is exposed in a http client.

**Prerequisites** 
* java 8 </br>
* Configuration of all property files [1]  </br>

**[1] Configuration Property files**  </br>
You have 8 property files, each file corresponds to a information page : </br>
* global.config.properties
* vi.config.properties 
* batteryvalues.config.properties
* checksystem.config.properties
* smaa.config.properties
* smab.config.properties
* statistic.config.properties
* additional.config.properties

In the first : **global.config.properties**, you must indicate : </br>
serveur: {IPserver, e.g. : http://isdiv326573:81/ }  </br>
pages= {pages : Battery_Values,Additional,Check_System,SMA_A,SMA_B,Statistic,VI}  </br>
samplingtimes= {sampling times of each page/ write thread: 2,2,2,2,2,2,2,{10}} --the last number corresponds to write thread's sampling time--  </br>
influxdb = {connection credentials for influxdb, e.g. : {http://localhost:8086/},{username},{password},{databsename}}  </br>

**Keep in mind do not put space after commas !** </br>

For the rest of files, you must write the delta values for each parameter. Please keep in mind, put points in decimal numbers. </br>
e.g. In **vi.config.properties**, you'r going to find : </br>
     vi.Average=0 </br>
     vi.Maximum=0 </br>
     vi.Minimum=0 ... </br>
In the last line in these property files, you must write all **"Not Number Parameters"**, e.g. : {vi.charging,st.date,st.time} </br>

We provide all these property files with the default values. </br>

**Deployment** </br>
In order to deploy, please execute in a command line : </br>
**java -jar batteryReader-1.2.jar** 

You should to watch in command line some logs like :  </br> 
 INFO [pool-1-thread-4] (BatteryReaderPageCtrl.java:115) - VI: 59110 size : 0               </br>
 INFO [pool-1-thread-3] (BatteryReaderPageCtrl.java:115) - Battery_Values: 59111 size : 1   </br>
 INFO [pool-1-thread-8] (BatteryReaderPageCtrl.java:115) - SMA_A: 59112 size : 2            </br>
 INFO [pool-1-thread-5] (BatteryReaderPageCtrl.java:115) - VI: 59112 size : 3               </br>
 INFO [pool-1-thread-6] (BatteryReaderPageCtrl.java:115) - Additional: 59113 size : 4       </br>
 INFO [pool-1-thread-1] (BatteryReaderPageCtrl.java:115) - SMA_B: 59113 size : 4            </br>
 INFO [pool-1-thread-2] (BatteryReaderPageCtrl.java:115) - VI: 59114 size : 4               </br>
 INFO [pool-1-thread-4] (LoaderInfluxDB.java:53) - -------------------------------Data to load : 4 Total Data : 4   </br>
 INFO [pool-1-thread-4] (LoaderInfluxDB.java:76) - -------------------------------Loaded Data : 4 Total Data : 0    </br>
 INFO [pool-1-thread-4] (BatteryReaderPageCtrl.java:127) - -----------------------Total Data : 0                    </br>

You could verify the data in your influx database with :    </br>
**CLI (influx)** --> use "databasename"                         </br>
                 --> select "field : V1, string1...." from "measurement like : VI, Battery_Values..." </br>
                 e.g.: select * from VI </br>
                 
**browser or HTTP Client** --> Write in the address bar : </br>
                           http://{influx.database.server}:{port_default_influx}/query?db={databasename}&username={admin}&password={admin}&q={query} </br>
                           e.g.: http://192.168.10.43:8086/query?db=influxdb&username=admin&password=admin&q=select%20*%20from%20VI      </br> 

**Built With** </br>
Maven

**Authors** </br>
Stephanya CASANOVA MARROQUIN - Initial work 

**License** </br>
IFPEN 

**2018**
