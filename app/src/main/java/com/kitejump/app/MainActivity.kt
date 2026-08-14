package com.kitejump.app

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

private val BG=Color(0xFF050A11); private val PANEL=Color(0xFF0D1823); private val PANEL2=Color(0xFF102231)
private val NEON=Color(0xFF24E7A8); private val CYAN=Color(0xFF4EC9FF); private val WHITE=Color(0xFFF4F8FB); private val MUTED=Color(0xFF8295A8); private val RED=Color(0xFFFF5365)

data class Jump(val number:Int,val height:Float,val airtime:Float,val speed:Float)

class MainActivity:ComponentActivity(){
    private val permission=registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){}
    override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState); permission.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION)); setContent{App()}}
}

@Composable fun App(){
    var tab by remember{mutableIntStateOf(0)}; var ride by remember{mutableStateOf(false)}; var sim by remember{mutableStateOf(false)}
    var h by remember{mutableFloatStateOf(0f)}; var air by remember{mutableFloatStateOf(0f)}; var spd by remember{mutableFloatStateOf(0f)}; var best by remember{mutableFloatStateOf(0f)}
    var jumps by remember{mutableStateOf(listOf<Jump>())}
    LaunchedEffect(ride,sim){if(ride&&sim){while(ride){delay(1600);h=2f+Random.nextFloat()*7f;air=1.6f+Random.nextFloat()*1.1f;spd=28f+Random.nextFloat()*18f;val j=Jump(jumps.size+1,h,air,spd);jumps=listOf(j)+jumps;best=max(best,h);delay(900);h=0f}}}
    MaterialTheme(colorScheme=darkColorScheme(background=BG,surface=PANEL,primary=NEON,secondary=CYAN)){
        Box(Modifier.fillMaxSize().background(BG)){when(tab){0->Home(ride,h,air,spd,best,jumps.size,sim,{ride=!ride},{sim=!sim});1->Jumps(jumps,best);2->Settings(sim){sim=it}};Bottom(tab){tab=it}}
    }
}

@Composable private fun Home(ride:Boolean,h:Float,air:Float,spd:Float,best:Float,count:Int,sim:Boolean,toggleRide:()->Unit,toggleSim:()->Unit){
    val ah by animateFloatAsState(h,label="h")
    Column(Modifier.fillMaxSize().padding(20.dp).padding(bottom=92.dp),horizontalAlignment=Alignment.CenterHorizontally){
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Column{Text("KITE JUMP",color=WHITE,fontSize=27.sp,fontWeight=FontWeight.Black);Text(if(ride)"LIVE SESSION" else "READY TO RIDE",color=NEON,fontSize=12.sp,fontWeight=FontWeight.Bold)};Icon(Icons.Default.Tune,null,tint=MUTED,modifier=Modifier.size(28.dp))}
        Spacer(Modifier.height(16.dp)); Box(Modifier.size(292.dp),contentAlignment=Alignment.Center){Canvas(Modifier.fillMaxSize()){val r=size.minDimension/2-18;drawArc(PANEL2,-140f,280f,false,style=Stroke(18f,cap=StrokeCap.Round));drawArc(NEON,-140f,min(ah/12f,1f)*280f,false,style=Stroke(18f,cap=StrokeCap.Round));for(i in 0..20){val a=Math.toRadians((-140+i*14).toDouble());val p1=Offset(center.x+(r-2)*kotlin.math.cos(a).toFloat(),center.y+(r-2)*kotlin.math.sin(a).toFloat());val p2=Offset(center.x+(r-10)*kotlin.math.cos(a).toFloat(),center.y+(r-10)*kotlin.math.sin(a).toFloat());drawLine(if(i%5==0)CYAN else Color(0xFF294052),p1,p2,2f)}};Column(horizontalAlignment=Alignment.CenterHorizontally){Text("%.1f".format(ah),color=WHITE,fontSize=65.sp,fontWeight=FontWeight.Black);Text("METERS",color=NEON,fontSize=13.sp,fontWeight=FontWeight.Bold,letterSpacing=2.sp);Text(if(ride&&h>.5f)"AIRBORNE" else "ALTITUDE",color=MUTED,fontSize=12.sp)}}
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)){Stat("AIRTIME",if(air>0)"%.2fs".format(air)else"—",Modifier.weight(1f));Stat("SPEED",if(spd>0)"%.1f".format(spd)else"—",Modifier.weight(1f),"km/h");Stat("BEST",if(best>0)"%.1fm".format(best)else"—",Modifier.weight(1f))}
        Spacer(Modifier.height(10.dp));Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)){Info("JUMPS",count.toString(),Icons.Default.FlightTakeoff,Modifier.weight(1f));Info("SENSORS",if(ride)"ACTIVE" else "READY",Icons.Default.Sensors,Modifier.weight(1f))}
        Spacer(Modifier.weight(1f));Button(onClick=toggleRide,modifier=Modifier.fillMaxWidth().height(64.dp).shadow(16.dp,RoundedCornerShape(22.dp)),shape=RoundedCornerShape(22.dp),colors=ButtonDefaults.buttonColors(containerColor=if(ride)RED else NEON,contentColor=Color.Black)){Icon(if(ride)Icons.Default.Stop else Icons.Default.PlayArrow,null);Spacer(Modifier.width(8.dp));Text(if(ride)"STOP RIDE" else "START RIDE",fontSize=20.sp,fontWeight=FontWeight.Black)}
        Row(verticalAlignment=Alignment.CenterVertically){Switch(checked=sim,onCheckedChange={toggleSim()},colors=SwitchDefaults.colors(checkedThumbColor=BG,checkedTrackColor=NEON));Text(" TEST / SIMULATION",color=MUTED,fontSize=11.sp,fontWeight=FontWeight.Bold)}
    }
}
@Composable private fun Stat(t:String,v:String,m:Modifier,s:String="")=Card(m,colors=CardDefaults.cardColors(containerColor=PANEL),shape=RoundedCornerShape(18.dp)){Column(Modifier.padding(13.dp)){Text(t,color=MUTED,fontSize=9.sp,fontWeight=FontWeight.Bold);Text(v,color=WHITE,fontSize=18.sp,fontWeight=FontWeight.Black);if(s.isNotEmpty())Text(s,color=NEON,fontSize=9.sp)}}
@Composable private fun Info(t:String,v:String,i:ImageVector,m:Modifier)=Card(m,colors=CardDefaults.cardColors(containerColor=PANEL2),shape=RoundedCornerShape(18.dp)){Row(Modifier.padding(14.dp),verticalAlignment=Alignment.CenterVertically){Icon(i,null,tint=NEON);Spacer(Modifier.width(9.dp));Column{Text(t,color=MUTED,fontSize=9.sp,fontWeight=FontWeight.Bold);Text(v,color=WHITE,fontWeight=FontWeight.Bold)}}}

@Composable private fun Jumps(j:List<Jump>,best:Float){Column(Modifier.fillMaxSize().padding(20.dp).padding(bottom=92.dp)){Text("JUMPS",color=WHITE,fontSize=30.sp,fontWeight=FontWeight.Black);Text("${j.size} jumps • best ${if(best>0)"%.1f m".format(best)else"—"}",color=MUTED);Spacer(Modifier.height(16.dp));if(j.isEmpty())Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){Text("No jumps yet.\nStart a ride to begin.",color=MUTED,textAlign=TextAlign.Center)}else LazyColumn(verticalArrangement=Arrangement.spacedBy(10.dp)){items(j){x->Card(colors=CardDefaults.cardColors(containerColor=PANEL),shape=RoundedCornerShape(20.dp)){Row(Modifier.fillMaxWidth().padding(17.dp),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(48.dp).background(Color(0xFF112A31),CircleShape),contentAlignment=Alignment.Center){Text("#${x.number}",color=NEON,fontWeight=FontWeight.Black)};Spacer(Modifier.width(13.dp));Column(Modifier.weight(1f)){Text("%.1f m".format(x.height),color=WHITE,fontSize=23.sp,fontWeight=FontWeight.Black);Text("AIRTIME %.2fs".format(x.airtime),color=MUTED,fontSize=11.sp)};Text("%.1f km/h".format(x.speed),color=CYAN,fontWeight=FontWeight.Bold)}}}}}}
@Composable private fun Settings(sim:Boolean,setSim:(Boolean)->Unit){Column(Modifier.fillMaxSize().padding(20.dp).padding(bottom=92.dp)){Text("SETTINGS",color=WHITE,fontSize=30.sp,fontWeight=FontWeight.Black);Text("Tune Kite Jump for your ride.",color=MUTED);Spacer(Modifier.height(18.dp));Setting("TEST MODE","Generate sample jumps for testing",sim,setSim);listOf("Units|Metric · meters / km/h","Barometer|Relative altitude sensor","GPS|Speed & track","Motion|Accelerometer + gyroscope","Minimum jump|1.5 m","Version|Kite Jump 1.0").forEach{val a=it.split("|");Row(Modifier.fillMaxWidth().padding(top=10.dp).background(PANEL2,RoundedCornerShape(18.dp)).padding(17.dp),horizontalArrangement=Arrangement.SpaceBetween){Text(a[0],color=WHITE,fontWeight=FontWeight.Bold);Text(a[1],color=NEON,fontSize=12.sp)}}}}
@Composable private fun Setting(t:String,s:String,c:Boolean,on:(Boolean)->Unit)=Card(colors=CardDefaults.cardColors(containerColor=PANEL),shape=RoundedCornerShape(20.dp)){Row(Modifier.fillMaxWidth().padding(18.dp),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text(t,color=NEON,fontWeight=FontWeight.Black);Text(s,color=MUTED,fontSize=12.sp)};Switch(checked=c,onCheckedChange=on)}}
@Composable private fun Bottom(sel:Int,on:(Int)->Unit){NavigationBar(containerColor=Color(0xFF08121B),tonalElevation=0.dp){listOf(Icons.Default.Speed to "RIDE",Icons.Default.FormatListNumbered to "JUMPS",Icons.Default.Settings to "SETTINGS").forEachIndexed{i,x->NavigationBarItem(selected=sel==i,onClick={on(i)},icon={Icon(x.first,null)},label={Text(x.second,fontSize=10.sp,fontWeight=FontWeight.Bold)},colors=NavigationBarItemDefaults.colors(selectedIconColor=BG,selectedTextColor=NEON,indicatorColor=NEON,unselectedIconColor=MUTED,unselectedTextColor=MUTED))}}}
