#include <fstream>
#include "ns3/core-module.h"
#include "ns3/internet-module.h"
#include "ns3/internet-apps-module.h"
#include "ns3/mobility-module.h"
#include "ns3/spectrum-module.h"
#include "ns3/propagation-module.h"
#include "ns3/sixlowpan-module.h"
#include "ns3/ipv6-flow-classifier.h"
#include "ns3/lr-wpan-module.h"
#include "ns3/csma-module.h"
#include "ns3/applications-module.h"
#include "ns3/flow-monitor-helper.h"
#include <ns3/lr-wpan-error-model.h>
#include "ns3/netanim-module.h"


using namespace ns3;


int main (int argc, char** argv) 
{
  uint32_t nCsma = 1;

  uint32_t lwNode = 4;
  uint32_t lwNode2 = 5;
  uint32_t lwNode3 = 7;


  CommandLine cmd (__FILE__);
  cmd.AddValue ("nCsma", "Number of Csma node", nCsma);
  cmd.AddValue ("lwNode", "Number of Left node", lwNode);
  cmd.AddValue ("lwNode2", "Number of Right node", lwNode2);
  cmd.AddValue ("lwNode3",  "Number of down node", lwNode3);


  cmd.Parse (argc, argv);

  uint32_t total_time = 100;

  uint32_t SentPackets = 0;
  uint32_t ReceivedPackets = 0;
  uint32_t LostPackets = 0;
  uint32_t flows = 0;

  NodeContainer Lw_nodes;
  Lw_nodes.Create (lwNode);

  NodeContainer Lw_nodes2;
  Lw_nodes2.Create (lwNode2);

  NodeContainer Lw_nodes3;
  Lw_nodes3.Create (lwNode3);


  NodeContainer Csma_Nodes;
  Csma_Nodes.Create (nCsma);
  Csma_Nodes.Add (Lw_nodes.Get (0));
  Csma_Nodes.Add (Lw_nodes2.Get (0));
  Csma_Nodes.Add (Lw_nodes3.Get (0));

  MobilityHelper mobility;
  MobilityHelper mobility2;
  MobilityHelper mobility3;

  uint32_t nodeSpeed  = 5;

  mobility.SetMobilityModel ("ns3::ConstantVelocityMobilityModel");
  
  mobility.SetPositionAllocator ("ns3::GridPositionAllocator",
                                 "MinX", DoubleValue (0.0),
                                 "MinY", DoubleValue (0.0),
                                 "DeltaX", DoubleValue (100),
                                 "DeltaY", DoubleValue (100),
                                 "GridWidth", UintegerValue (10),
                                 "LayoutType", StringValue ("RowFirst"));

  mobility2.SetMobilityModel ("ns3::ConstantVelocityMobilityModel");
  
  mobility2.SetPositionAllocator ("ns3::GridPositionAllocator",
                                 "MinX", DoubleValue (0.0),
                                 "MinY", DoubleValue (0.0),
                                 "DeltaX", DoubleValue (70),
                                 "DeltaY", DoubleValue (70),
                                 "GridWidth", UintegerValue (10),
                                 "LayoutType", StringValue ("RowFirst"));       

  mobility3.SetMobilityModel ("ns3::ConstantVelocityMobilityModel");
  
  mobility3.SetPositionAllocator ("ns3::GridPositionAllocator",
                                 "MinX", DoubleValue (0.0),
                                 "MinY", DoubleValue (0.0),
                                 "DeltaX", DoubleValue (50),
                                 "DeltaY", DoubleValue (50),
                                 "GridWidth", UintegerValue (10),
                                 "LayoutType", StringValue ("RowFirst"));                         
                              
                
  mobility.Install (Lw_nodes);
  mobility2.Install (Lw_nodes2);
  mobility3.Install (Lw_nodes3);

  Ptr<ConstantVelocityMobilityModel> mob = Lw_nodes.Get(0)->GetObject<ConstantVelocityMobilityModel>();
  mob->SetVelocity(Vector(nodeSpeed , 0, 0));

  Ptr<ConstantVelocityMobilityModel> mob2 = Lw_nodes2.Get(0)->GetObject<ConstantVelocityMobilityModel>();
  mob2->SetVelocity(Vector(nodeSpeed , 0, 0));

  Ptr<ConstantVelocityMobilityModel> mob3 = Lw_nodes3.Get(0)->GetObject<ConstantVelocityMobilityModel>();
  mob3->SetVelocity(Vector(nodeSpeed , 0, 0));


  LrWpanHelper lrWifi;
  NetDeviceContainer lrDevices = lrWifi.Install (Lw_nodes);
  lrWifi.AssociateToPan (lrDevices, 0);

  LrWpanHelper lrWifi2;
  NetDeviceContainer lrDevices2 = lrWifi2.Install (Lw_nodes2);
  lrWifi2.AssociateToPan (lrDevices2, 0);

  LrWpanHelper lrWifi3;
  NetDeviceContainer lrDevices3 = lrWifi2.Install (Lw_nodes3);
  lrWifi3.AssociateToPan (lrDevices3, 0);

  InternetStackHelper ISv6;
  ISv6.Install (Lw_nodes);
  ISv6.Install (Lw_nodes2);
  ISv6.Install (Lw_nodes3);
  ISv6.Install (Csma_Nodes.Get (0));


  SixLowPanHelper SixLow;
  NetDeviceContainer SixLow_devices = SixLow.Install (lrDevices);

  SixLowPanHelper SixLow2;
  NetDeviceContainer SixLow_devices2 = SixLow2.Install (lrDevices2);

  SixLowPanHelper SixLow3;
  NetDeviceContainer SixLow_devices3 = SixLow3.Install (lrDevices3);

  CsmaHelper csmaHelp;
  NetDeviceContainer csmDevices = csmaHelp.Install (Csma_Nodes);

  Ipv6AddressHelper ipv6;

  ipv6.SetBase (Ipv6Address ("2022:f00f::"), Ipv6Prefix (64));
  Ipv6InterfaceContainer lr_interfaces3;
  lr_interfaces3 = ipv6.Assign (SixLow_devices3);
  lr_interfaces3.SetForwarding (3, true);
  lr_interfaces3.SetDefaultRouteInAllNodes (2);

  ipv6.SetBase (Ipv6Address ("2022:f00e::"), Ipv6Prefix (64));
  Ipv6InterfaceContainer lr_interfaces2;
  lr_interfaces2 = ipv6.Assign (SixLow_devices2);
  lr_interfaces2.SetForwarding (2, true);
  lr_interfaces2.SetDefaultRouteInAllNodes (2);

  ipv6.SetBase (Ipv6Address ("2022:f00d::"), Ipv6Prefix (64));
  Ipv6InterfaceContainer lr_interfaces;
  lr_interfaces = ipv6.Assign (SixLow_devices);
  lr_interfaces.SetForwarding (0, true);
  lr_interfaces.SetDefaultRouteInAllNodes (0);

  ipv6.SetBase (Ipv6Address ("2022:cafe::"), Ipv6Prefix (64));
  Ipv6InterfaceContainer c_interfaces;
  c_interfaces = ipv6.Assign (csmDevices);
  c_interfaces.SetForwarding (1, true);
  c_interfaces.SetDefaultRouteInAllNodes (1);

  for (uint32_t i = 0; i < SixLow_devices.GetN (); i++) {
    Ptr<NetDevice> device = SixLow_devices.Get (i);
    device->SetAttribute ("UseMeshUnder", BooleanValue (true));
    device->SetAttribute ("MeshUnderRadius", UintegerValue (10));
  }

  for (uint32_t i = 0; i < SixLow_devices2.GetN (); i++) {
    Ptr<NetDevice> device = SixLow_devices2.Get (i);
    device->SetAttribute ("UseMeshUnder", BooleanValue (true));
    device->SetAttribute ("MeshUnderRadius", UintegerValue (10));
  }

  for (uint32_t i = 0; i < SixLow_devices3.GetN (); i++) {
    Ptr<NetDevice> device = SixLow_devices3.Get (i);
    device->SetAttribute ("UseMeshUnder", BooleanValue (true));
    device->SetAttribute ("MeshUnderRadius", UintegerValue (10));
  }

  FlowMonitorHelper flowmon;
  Ptr<FlowMonitor> monitor = flowmon.InstallAll();

  uint32_t ports = 9;
  uint32_t pSize = 100;

  for( uint32_t i=1; i<=lwNode-1; i++ ) {
    BulkSendHelper sourceApp ("ns3::TcpSocketFactory",
                              Inet6SocketAddress (c_interfaces.GetAddress (0, 1), 
                              ports+i));
    sourceApp.SetAttribute ("SendSize", UintegerValue (pSize));

    ApplicationContainer sourceApps = sourceApp.Install (Lw_nodes.Get (i));
    sourceApps.Start (Seconds(0));
    sourceApps.Stop (Seconds(total_time));

    PacketSinkHelper sinkApp ("ns3::TcpSocketFactory",
    Inet6SocketAddress (Ipv6Address::GetAny (), ports+i));
    sinkApp.SetAttribute ("Protocol", TypeIdValue (TcpSocketFactory::GetTypeId ()));
    ApplicationContainer sinkApps = sinkApp.Install (Csma_Nodes.Get(0));
    sinkApps.Start (Seconds (0.0));
    sinkApps.Stop (Seconds (total_time));
    
    ports++;
  }
  

  for( uint32_t i=1; i<=lwNode2-1; i++ ) {
    BulkSendHelper sourceApp ("ns3::TcpSocketFactory",
                              Inet6SocketAddress (lr_interfaces2.GetAddress (i, 1), 
                              ports+i));
    sourceApp.SetAttribute ("SendSize", UintegerValue (pSize));
    ApplicationContainer sourceApps = sourceApp.Install (Csma_Nodes.Get (0));
    sourceApps.Start (Seconds(0));
    sourceApps.Stop (Seconds(total_time));

    PacketSinkHelper sinkApp ("ns3::TcpSocketFactory",
    Inet6SocketAddress (Ipv6Address::GetAny (), ports+i));
    sinkApp.SetAttribute ("Protocol", TypeIdValue (TcpSocketFactory::GetTypeId ()));
    ApplicationContainer sinkApps = sinkApp.Install (Lw_nodes2.Get(i));
    sinkApps.Start (Seconds (10.0));
    sinkApps.Stop (Seconds (total_time));
    
    ports++;
  }

  for( uint32_t i=1; i<=lwNode3-lwNode3; i++ ) {
    BulkSendHelper sourceApp ("ns3::TcpSocketFactory",
                              Inet6SocketAddress (lr_interfaces3.GetAddress (i, 1), 
                              ports+i));
    sourceApp.SetAttribute ("SendSize", UintegerValue (pSize));
    ApplicationContainer sourceApps = sourceApp.Install (Csma_Nodes.Get (0));
    sourceApps.Start (Seconds(0));
    sourceApps.Stop (Seconds(total_time));

    PacketSinkHelper sinkApp ("ns3::TcpSocketFactory",
    Inet6SocketAddress (Ipv6Address::GetAny (), ports+i));
    sinkApp.SetAttribute ("Protocol", TypeIdValue (TcpSocketFactory::GetTypeId ()));
    ApplicationContainer sinkApps = sinkApp.Install (Lw_nodes3.Get(i));
    sinkApps.Start (Seconds (10.0));
    sinkApps.Stop (Seconds (total_time));
    
    ports++;
  }

  //AnimationInterface anim ("TaskA-Wireless-Mobile-Low-Rate-802.11-NetAnim.xml");


  Simulator::Stop (Seconds (total_time));
  Simulator::Run ();

  int j=0 ;
  float AvgThroughput = 0;
  Time Delay, Jitter ;

  std::string m_CSVfileName2="TaskA-Wireless-Mobile-Low-Rate-802.11_2-E.csv";

  std::ofstream out2 (m_CSVfileName2.c_str (), std::ios::app);

  Ptr<Ipv6FlowClassifier> classifier = DynamicCast<Ipv6FlowClassifier> (flowmon.GetClassifier ());
    std::map<FlowId, FlowMonitor::FlowStats> stats = monitor->GetFlowStats ();

    for (std::map<FlowId, FlowMonitor::FlowStats>::const_iterator iter = stats.begin (); iter != stats.end (); ++iter)
      {
      //Ipv6FlowClassifier::FiveTuple t = classifier->FindFlow (iter->first);

      out2 << (Simulator::Now ()).GetSeconds () << ","
        << iter->first << ","  // Flow ID
        << iter->second.rxPackets*100/iter->second.txPackets << "," // Packet delivery ratio
        << (iter->second.txPackets-iter->second.rxPackets)*100/iter->second.txPackets << "," // Packet loss ratio
        << iter->second.delaySum << "," // Delay
        << iter->second.rxBytes * 8.0/(iter->second.timeLastRxPacket.GetSeconds()-iter->second.timeFirstTxPacket.GetSeconds())/1024 << "," // Throughput
        << iter->second.jitterSum<< "" //Jitter
        << std::endl;

  SentPackets = SentPackets +(iter->second.txPackets);
  ReceivedPackets = ReceivedPackets + (iter->second.rxPackets);
  LostPackets = LostPackets + (iter->second.txPackets-iter->second.rxPackets);
  Delay = Delay + (iter->second.delaySum);

  if(iter->second.rxPackets)
  {
  flows=flows+1;
  AvgThroughput = AvgThroughput + (iter->second.rxBytes * 8.0/(iter->second.timeLastRxPacket.GetSeconds()-iter->second.timeFirstTxPacket.GetSeconds())/1024);
  }
  Jitter = Jitter + (iter->second.jitterSum);

  j = j + 1;

  }

  AvgThroughput = AvgThroughput/j;
  NS_LOG_UNCOND("\n--------Simulation Results Given Bellow----------"<<std::endl);
  NS_LOG_UNCOND("-------- Student id : 1705087 ----------"<<std::endl);
  NS_LOG_UNCOND("Flows = " << flows<< " ");
  NS_LOG_UNCOND("Average Throughput =" << AvgThroughput<< "Kbps");
  NS_LOG_UNCOND("End to End Delay =" << Delay);
  NS_LOG_UNCOND("Packet delivery ratio =" << ((ReceivedPackets*100)/SentPackets)<< "%");
  NS_LOG_UNCOND("Packet Loss ratio =" << ((LostPackets*100)/SentPackets)<< "%");
  NS_LOG_UNCOND("End to End Jitter delay =" << Jitter);
  monitor->SerializeToXmlFile("TaskA-Wireless-Mobile-Low-Rate-802.11_2-E.xml", true, true);

  out2.close ();

  Simulator::Destroy ();

  return 0;
}

