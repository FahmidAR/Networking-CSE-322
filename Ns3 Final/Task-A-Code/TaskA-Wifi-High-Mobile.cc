#include <fstream>
#include <iostream>
#include "ns3/core-module.h"
#include "ns3/mobility-module.h"
#include "ns3/aodv-module.h"
#include "ns3/dsr-module.h"
#include "ns3/applications-module.h"
#include "ns3/network-module.h"
#include "ns3/point-to-point-module.h"
#include "ns3/internet-module.h"
#include "ns3/yans-wifi-helper.h"
#include "ns3/netanim-module.h"
#include "ns3/olsr-module.h"
#include "ns3/dsdv-module.h"
#include "ns3/flow-monitor-module.h"


using namespace ns3;
using namespace dsr;
uint32_t flowTotal=0;

NS_LOG_COMPONENT_DEFINE ("TaskA-Wireless-Mobile-High-Rate-802.11");


class RoutingExperiment
{
public:
  RoutingExperiment ();

  void Run (int nSinks, double txp, std::string CSVfileName,int Wifis, int Wifis2,int Speed);
  
  //std::string CommandSetup (int argc, char **argv);
  //uint32_t flowTotal;

private:
  Ptr<Socket> SetupPacketReceive (Ipv4Address addr, Ptr<Node> node);
  void ReceivePacket (Ptr<Socket> socket);
  void CheckThroughput ();

  uint32_t port;
  uint32_t bytesTotal;
  uint32_t packetsReceived;

  std::string m_CSVfileName;
  int m_nSinks;
  double m_txp;
};

RoutingExperiment::RoutingExperiment ()
  : port (9),
    bytesTotal (0),
    //flowTotal (0),
    packetsReceived (0),
    m_CSVfileName ("TaskA-Wireless-Mobile-High-Rate-802.11.csv")
{
}

static inline std::string
PrintReceivedPacket (Ptr<Socket> socket, Ptr<Packet> packet, Address senderAddress)
{
  std::ostringstream oss;
  flowTotal +=1;

  oss << "Time (sec) = "<< Simulator::Now ().GetSeconds () << " Node = " << socket->GetNode ()->GetId ();

  if (InetSocketAddress::IsMatchingType (senderAddress))
    {
      InetSocketAddress addr = InetSocketAddress::ConvertFrom (senderAddress);
      oss << " received 1 packet from Ip = " << addr.GetIpv4 ();
    }
  else
    {
      oss << " received 1 packet !";
    }
  return oss.str ();
}

void
RoutingExperiment::ReceivePacket (Ptr<Socket> socket)
{
  Ptr<Packet> packet;
  Address senderAddress;
  while ((packet = socket->RecvFrom (senderAddress)))
    {
      bytesTotal += packet->GetSize ();
      packetsReceived += 1;
      NS_LOG_UNCOND (PrintReceivedPacket (socket, packet, senderAddress));
    }
}

void
RoutingExperiment::CheckThroughput ()
{
  double kbs = (bytesTotal * 8.0) / 1000;
  bytesTotal = 0;

  std::ofstream out (m_CSVfileName.c_str (), std::ios::app);

  out << (Simulator::Now ()).GetSeconds () << ","
      << kbs << ","
      << packetsReceived << ","
      << m_nSinks << " "
      << std::endl;

  out.close ();
  packetsReceived = 0;
  Simulator::Schedule (Seconds (1.0), &RoutingExperiment::CheckThroughput, this);
}

Ptr<Socket>
RoutingExperiment::SetupPacketReceive (Ipv4Address addr, Ptr<Node> node)
{
  TypeId tid = TypeId::LookupByName ("ns3::UdpSocketFactory");
  Ptr<Socket> sink = Socket::CreateSocket (node, tid);
  InetSocketAddress local = InetSocketAddress (addr, port);
  sink->Bind (local);
  sink->SetRecvCallback (MakeCallback (&RoutingExperiment::ReceivePacket, this));

  return sink;
}


int
main (int argc, char *argv[])
{
  int nSinks = 5;
  double txp = 7.5;
  int Wifis = 20;
  int Wifis2 = 20;
  int Speed = 20;

  RoutingExperiment experiment;

  CommandLine cmd (__FILE__);
  cmd.AddValue ("nSinks", "Number of Sinks node", nSinks);
  cmd.AddValue ("Wifi", "Number of Left Manet node", Wifis);
  cmd.AddValue ("Wifis2", "Number of Right Manet node", Wifis2);
  cmd.AddValue ("Speed", "Speed of Manet node", Speed);

  std::string CSVfileName = "TaskA-Wireless-Mobile-High-Rate-802.11.csv";

  cmd.Parse (argc, argv);

  //blank out the last output file and write the column headers
  std::ofstream out (CSVfileName.c_str ());
  /*out << "SimulationSecond," <<
  "ReceiveRate," <<
  "PacketsReceived," <<
  "NumberOfSinks," <<
  std::endl;*/
  out.close ();

  experiment.Run (nSinks, txp, CSVfileName , Wifis , Wifis2 , Speed);
}

void
RoutingExperiment::Run (int nSinks, double txp, std::string CSVfileName ,int Wifis, int Wifis2 , int Speed)
{
  Packet::EnablePrinting ();
  m_nSinks = nSinks;
  m_txp = txp;
  m_CSVfileName = CSVfileName;

  int nWifis = Wifis;
  int nWifis2 = Wifis2;

  double TotalTime = 120.0;
  std::string rate ("2048bps");
  std::string phyMode ("DsssRate11Mbps");
  std::string tr_name ("TaskA-Wireless-Mobile-High-Rate-802.11");
  int nodeSpeed = Speed; //in m/s
  int nodePause = 0; //in s

  uint32_t SentPackets = 0;
  uint32_t ReceivedPackets = 0;
  uint32_t LostPackets = 0;

  Config::SetDefault  ("ns3::OnOffApplication::PacketSize",StringValue ("64"));
  Config::SetDefault ("ns3::OnOffApplication::DataRate",  StringValue (rate));

  //Set Non-unicastMode rate to unicast mode
  Config::SetDefault ("ns3::WifiRemoteStationManager::NonUnicastMode",StringValue (phyMode));

  NodeContainer p2pNodes;
  //p2pNodes.Create (2);

  NodeContainer adhocNodes;
  adhocNodes.Create (nWifis);
  //NodeContainer wifiApNodes = p2pNodes.Get (0);
  p2pNodes.Add(adhocNodes.Get(0));

  NodeContainer adhocNodes2;
  adhocNodes2.Create (nWifis2);
  //NodeContainer wifiApNodes2 = p2pNodes.Get (1);
  p2pNodes.Add(adhocNodes2.Get(0));


  PointToPointHelper pointToPoint;
  pointToPoint.SetDeviceAttribute ("DataRate", StringValue ("5Mbps"));
  pointToPoint.SetChannelAttribute ("Delay", StringValue ("2ms"));

  NetDeviceContainer p2pDevices;
  p2pDevices = pointToPoint.Install (p2pNodes);

  // setting up wifi phy and channel using helpers
  WifiHelper wifi;
  wifi.SetStandard (WIFI_STANDARD_80211b);

  WifiHelper wifi2;
  wifi2.SetStandard (WIFI_STANDARD_80211b);

  YansWifiPhyHelper wifiPhy;
  YansWifiChannelHelper wifiChannel;
  wifiChannel.SetPropagationDelay ("ns3::ConstantSpeedPropagationDelayModel");
  wifiChannel.AddPropagationLoss ("ns3::FriisPropagationLossModel");
  wifiPhy.SetChannel (wifiChannel.Create ());

  YansWifiPhyHelper wifiPhy2;
  YansWifiChannelHelper wifiChannel2;
  wifiChannel2.SetPropagationDelay ("ns3::ConstantSpeedPropagationDelayModel");
  wifiChannel2.AddPropagationLoss ("ns3::FriisPropagationLossModel");
  wifiPhy2.SetChannel (wifiChannel2.Create ());

  

  // Add a mac and disable rate control
  WifiMacHelper wifiMac , macAp;
  wifi.SetRemoteStationManager ("ns3::ConstantRateWifiManager",
                                "DataMode",StringValue (phyMode),
                                "ControlMode",StringValue (phyMode));

  wifiPhy.Set ("TxPowerStart",DoubleValue (txp));
  wifiPhy.Set ("TxPowerEnd", DoubleValue (txp));

  wifiMac.SetType ("ns3::AdhocWifiMac");
  //macAp.SetType ("ns3::ApWifiMac");

  NetDeviceContainer adhocDevices = wifi.Install (wifiPhy, wifiMac, adhocNodes);
  //NetDeviceContainer wifiApDevices = wifi.Install (wifiPhy, macAp, wifiApNodes);

  WifiMacHelper wifiMac2 , macAp2;
  wifi2.SetRemoteStationManager ("ns3::ConstantRateWifiManager",
                                "DataMode",StringValue (phyMode),
                                "ControlMode",StringValue (phyMode));

  wifiPhy2.Set ("TxPowerStart",DoubleValue (txp));
  wifiPhy2.Set ("TxPowerEnd", DoubleValue (txp));

  wifiMac2.SetType ("ns3::AdhocWifiMac");
  //macAp2.SetType ("ns3::ApWifiMac");



  NetDeviceContainer adhocDevices2 = wifi.Install (wifiPhy2, wifiMac2, adhocNodes2);
  //NetDeviceContainer wifiApDevices2 = wifi2.Install (wifiPhy2, macAp2, wifiApNodes2);

  

  MobilityHelper mobilityAdhoc;
  int64_t streamIndex = 0; // used to get consistent mobility across scenarios

  ObjectFactory pos;
  pos.SetTypeId ("ns3::RandomRectanglePositionAllocator");
  pos.Set ("X", StringValue ("ns3::UniformRandomVariable[Min=0.0|Max=300.0]"));
  pos.Set ("Y", StringValue ("ns3::UniformRandomVariable[Min=0.0|Max=1500.0]"));

  Ptr<PositionAllocator> taPositionAlloc = pos.Create ()->GetObject<PositionAllocator> ();
  streamIndex += taPositionAlloc->AssignStreams (streamIndex);

  std::stringstream ssSpeed;
  ssSpeed << "ns3::UniformRandomVariable[Min=0.0|Max=" << nodeSpeed << "]";
  std::stringstream ssPause;
  ssPause << "ns3::ConstantRandomVariable[Constant=" << nodePause << "]";
  mobilityAdhoc.SetMobilityModel ("ns3::RandomWaypointMobilityModel",
                                  "Speed", StringValue (ssSpeed.str ()),
                                  "Pause", StringValue (ssPause.str ()),
                                  "PositionAllocator", PointerValue (taPositionAlloc));
  mobilityAdhoc.SetPositionAllocator (taPositionAlloc);
  mobilityAdhoc.Install (adhocNodes);
  streamIndex += mobilityAdhoc.AssignStreams (adhocNodes, streamIndex);

  NS_UNUSED (streamIndex); // From this point, streamIndex is unused

  MobilityHelper mobilityAdhoc2;
  int64_t streamIndex2 = 0; // used to get consistent mobility across scenarios

  ObjectFactory pos2;
  pos2.SetTypeId ("ns3::RandomRectanglePositionAllocator");
  pos2.Set ("X", StringValue ("ns3::UniformRandomVariable[Min=500.0|Max=800.0]"));
  pos2.Set ("Y", StringValue ("ns3::UniformRandomVariable[Min=0.0|Max=1500.0]"));

  Ptr<PositionAllocator> taPositionAlloc2 = pos2.Create ()->GetObject<PositionAllocator> ();
  streamIndex2 += taPositionAlloc2->AssignStreams (streamIndex2);

  std::stringstream ssSpeed2;
  ssSpeed2 << "ns3::UniformRandomVariable[Min=0.0|Max=" << nodeSpeed << "]";
  std::stringstream ssPause2;
  ssPause2 << "ns3::ConstantRandomVariable[Constant=" << nodePause << "]";
  mobilityAdhoc2.SetMobilityModel ("ns3::RandomWaypointMobilityModel",
                                  "Speed", StringValue (ssSpeed.str ()),
                                  "Pause", StringValue (ssPause.str ()),
                                  "PositionAllocator", PointerValue (taPositionAlloc));
  mobilityAdhoc2.SetPositionAllocator (taPositionAlloc2);
  mobilityAdhoc2.Install (adhocNodes2);
  streamIndex2 += mobilityAdhoc2.AssignStreams (adhocNodes2, streamIndex2);

  NS_UNUSED (streamIndex2); // From this point, streamIndex is unused

  AodvHelper aodv;
  Ipv4ListRoutingHelper list;
  InternetStackHelper internet;

  //internet.Install(p2pNodes);

 
  list.Add (aodv, 100);
  internet.SetRoutingHelper (list);
  internet.Install (adhocNodes);
  internet.Install (adhocNodes2);

  NS_LOG_INFO ("assigning ip address");

  

  Ipv4AddressHelper addressAdhoc;

  addressAdhoc.SetBase ("10.1.3.0", "255.255.255.0");
  Ipv4InterfaceContainer p2pInterfaces;
  p2pInterfaces = addressAdhoc.Assign (p2pDevices);


  addressAdhoc.SetBase ("10.1.1.0", "255.255.255.0");
  Ipv4InterfaceContainer adhocApInterfaces,adhocInterfaces;
  adhocInterfaces = addressAdhoc.Assign (adhocDevices);
  //adhocApInterfaces = addressAdhoc.Assign (wifiApDevices);

  addressAdhoc.SetBase ("10.1.2.0", "255.255.255.0");
  Ipv4InterfaceContainer adhocApInterfaces2,adhocInterfaces2;
  adhocInterfaces2 = addressAdhoc.Assign (adhocDevices2);
  //adhocApInterfaces2 = addressAdhoc.Assign (wifiApDevices2);

  OnOffHelper onoff1 ("ns3::UdpSocketFactory",Address ());
  onoff1.SetAttribute ("OnTime", StringValue ("ns3::ConstantRandomVariable[Constant=1.0]"));
  onoff1.SetAttribute ("OffTime", StringValue ("ns3::ConstantRandomVariable[Constant=0.0]"));

  OnOffHelper onoff2 ("ns3::UdpSocketFactory",Address ());
  onoff2.SetAttribute ("OnTime", StringValue ("ns3::ConstantRandomVariable[Constant=1.0]"));
  onoff2.SetAttribute ("OffTime", StringValue ("ns3::ConstantRandomVariable[Constant=0.0]"));

  for (int i = 0; i < nSinks; i++)
    {
      Ptr<Socket> sink = SetupPacketReceive (adhocInterfaces.GetAddress (i), adhocNodes.Get (i));

      AddressValue remoteAddress (InetSocketAddress (adhocInterfaces.GetAddress (i), port));
      onoff1.SetAttribute ("Remote", remoteAddress);

      Ptr<UniformRandomVariable> var = CreateObject<UniformRandomVariable> ();
      ApplicationContainer temp = onoff1.Install (adhocNodes.Get (i + nSinks));

      Ptr<Socket> sink2 = SetupPacketReceive (adhocInterfaces2.GetAddress (i), adhocNodes2.Get (i));

      AddressValue remoteAddress2 (InetSocketAddress (adhocInterfaces2.GetAddress (i), port));
      onoff2.SetAttribute ("Remote", remoteAddress2);

      Ptr<UniformRandomVariable> var2 = CreateObject<UniformRandomVariable> ();
      ApplicationContainer temp2 = onoff2.Install (adhocNodes2.Get (i + nSinks));

      temp.Start (Seconds (var->GetValue (80.0,81.0)));
      temp.Stop (Seconds (TotalTime));

      temp2.Start (Seconds (var->GetValue (90.0,91.0)));
      temp2.Stop (Seconds (TotalTime));
    }
  
  /*for (int i = 0; i < nSinks; i++)
    {
      Ptr<Socket> sink2 = SetupPacketReceive (adhocInterfaces2.GetAddress (i), adhocNodes2.Get (i));

      AddressValue remoteAddress2 (InetSocketAddress (adhocInterfaces2.GetAddress (i), port));
      onoff2.SetAttribute ("Remote", remoteAddress2);

      Ptr<UniformRandomVariable> var2 = CreateObject<UniformRandomVariable> ();
      ApplicationContainer temp2 = onoff2.Install (adhocNodes2.Get (i + nSinks));
      temp2.Start (Seconds (var2->GetValue (100.0,101.0)));
      temp2.Stop (Seconds (TotalTime));
    }*/

  std::stringstream ss;
  ss << nWifis;
  std::string nodes = ss.str ();

  std::stringstream ss2;
  ss2 << nodeSpeed;
  std::string sNodeSpeed = ss2.str ();

  std::stringstream ss3;
  ss3 << nodePause;
  std::string sNodePause = ss3.str ();

  std::stringstream ss4;
  ss4 << rate;
  std::string sRate = ss4.str ();

  std::stringstream sss;
  sss << nWifis2;
  std::string nodes2 = sss.str ();

  

  /*std::stringstream sss2;
  sss2 << nodeSpeed;
  std::string sNodeSpeed2 = sss2.str ();

  std::stringstream sss3;
  sss3 << nodePause;
  std::string sNodePause2 = sss3.str ();

  std::stringstream sss4;
  sss4 << rate;
  std::string sRate2 = sss4.str ();*/

  //NS_LOG_INFO ("Configure Tracing.");
  //tr_name = tr_name + "_" + m_protocolName +"_" + nodes + "nodes_" + sNodeSpeed + "speed_" + sNodePause + "pause_" + sRate + "rate";

  //AsciiTraceHelper ascii;
  //Ptr<OutputStreamWrapper> osw = ascii.CreateFileStream ( (tr_name + ".tr").c_str());
  //wifiPhy.EnableAsciiAll (osw);
  AsciiTraceHelper ascii;
  MobilityHelper::EnableAsciiAll (ascii.CreateFileStream (tr_name + ".mob"));

  
  //Ptr<FlowMonitor> flowmon;
  //FlowMonitorHelper flowmonHelper;
  //flowmon = flowmonHelper.InstallAll ();

  FlowMonitorHelper flowmon;
  Ptr<FlowMonitor> monitor = flowmon.InstallAll();


  NS_LOG_INFO ("Run Simulation.");

  CheckThroughput ();

  AnimationInterface anim ("TaskA-Wireless-Mobile-High-Rate-802.11-NetAnim.xml");

  Simulator::Stop (Seconds (TotalTime));
  Simulator::Run ();

  int j=0;
  float AvgThroughput = 0;
  Time Delay;

  std::string m_CSVfileName2="TaskA-Wireless-Mobile-High-Rate-802.11_2.csv";

  std::ofstream out2 (m_CSVfileName2.c_str (), std::ios::app);

  Ptr<Ipv4FlowClassifier> classifier = DynamicCast<Ipv4FlowClassifier> (flowmon.GetClassifier ());
    std::map<FlowId, FlowMonitor::FlowStats> stats = monitor->GetFlowStats ();

    for (std::map<FlowId, FlowMonitor::FlowStats>::const_iterator iter = stats.begin (); iter != stats.end (); ++iter)
      {
      Ipv4FlowClassifier::FiveTuple t = classifier->FindFlow (iter->first);

      out2 << (Simulator::Now ()).GetSeconds () << ","
        << iter->first << ","  // Flow ID
        << t.sourceAddress << "," // Src Addr
        << t.destinationAddress << ","// des addr
        << iter->second.rxPackets*100/iter->second.txPackets << "," // Packet delivery ratio
        << (iter->second.txPackets-iter->second.rxPackets)*100/iter->second.txPackets << "," // Packet loss ratio
        << iter->second.delaySum << "," // Delay
        << iter->second.rxBytes * 8.0/(iter->second.timeLastRxPacket.GetSeconds()-iter->second.timeFirstTxPacket.GetSeconds())/1024 << "" // Throughput
        << std::endl;

  SentPackets = SentPackets +(iter->second.txPackets);
  ReceivedPackets = ReceivedPackets + (iter->second.rxPackets);
  LostPackets = LostPackets + (iter->second.txPackets-iter->second.rxPackets);
  AvgThroughput = AvgThroughput + (iter->second.rxBytes * 8.0/(iter->second.timeLastRxPacket.GetSeconds()-iter->second.timeFirstTxPacket.GetSeconds())/1024);
  Delay = Delay + (iter->second.delaySum);

  j = j + 1;

  }
   
  AvgThroughput = AvgThroughput/j;
  NS_LOG_UNCOND("\n--------Simulation Results Given Bellow----------"<<std::endl);
  NS_LOG_UNCOND("-------- Student id : 1705087 ----------"<<std::endl);
  NS_LOG_UNCOND("Total flow number  = " << flowTotal);
  NS_LOG_UNCOND("Average Throughput =" << AvgThroughput<< "Kbps");
  NS_LOG_UNCOND("End to End Delay =" << Delay);
  NS_LOG_UNCOND("Packet delivery ratio =" << ((ReceivedPackets*100)/SentPackets)<< "%");
  NS_LOG_UNCOND("Packet Loss ratio =" << ((LostPackets*100)/SentPackets)<< "%");
  monitor->SerializeToXmlFile("TaskA-Wireless-Mobile-High-Rate-802.11-2.xml", true, true);

  out2.close ();

  //std::cout <<"meo"<<std::endl;

  //flowmon->SerializeToXmlFile ((tr_name + ".flowmon").c_str(), false, false);

  Simulator::Destroy ();
}

