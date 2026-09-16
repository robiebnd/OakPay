"use client";

import { Bell, LockKeyhole, Settings as SettingsIcon, ShieldCheck, CheckCircle2 } from "lucide-react";
import { useEffect, useState } from "react";
import AdminShell from "../../components/AdminShell";

const KEY="payoak.admin.settings";
type SettingsState={operationalAlerts:boolean; securityPrompts:boolean; autoRefresh:boolean};
const defaults:SettingsState={operationalAlerts:true,securityPrompts:true,autoRefresh:true};

export default function SettingsPage(){
 const [settings,setSettings]=useState<SettingsState>(defaults); const [saved,setSaved]=useState(false);
 useEffect(()=>{try{const raw=localStorage.getItem(KEY);if(raw)setSettings({...defaults,...JSON.parse(raw)});}catch{}},[]);
 function update<K extends keyof SettingsState>(key:K,value:boolean){const next={...settings,[key]:value};setSettings(next);localStorage.setItem(KEY,JSON.stringify(next));setSaved(true);window.setTimeout(()=>setSaved(false),1200);}
 return <AdminShell><div className="space-y-8">
  <div><p className="text-xs font-bold uppercase tracking-[0.14em] text-[#397b0a]">Administration</p><h1 className="mt-1 text-3xl font-extrabold tracking-tight text-[#111827]">Settings</h1><p className="mt-2 max-w-2xl text-sm leading-6 text-[#6b7280]">Configure PayOak administration preferences and operational controls.</p></div>
  {saved?<div className="inline-flex items-center gap-2 rounded-xl bg-green-50 px-4 py-2 text-sm font-bold text-green-700"><CheckCircle2 size={16}/>Saved locally</div>:null}
  <div className="grid gap-5 lg:grid-cols-2"><SettingCard icon={<ShieldCheck size={21}/>} title="Security" description="Control administrator security prompts in this browser." on={settings.securityPrompts} onChange={v=>update("securityPrompts",v)}/><SettingCard icon={<Bell size={21}/>} title="Notifications" description="Enable operational alert indicators in the administration interface." on={settings.operationalAlerts} onChange={v=>update("operationalAlerts",v)}/><SettingCard icon={<LockKeyhole size={21}/>} title="Access Control" description="Access control remains enforced by the live authentication service." locked/><SettingCard icon={<SettingsIcon size={21}/>} title="System Preferences" description="Control live dashboard refresh behavior for this administrator session." on={settings.autoRefresh} onChange={v=>update("autoRefresh",v)}/></div>
  <section className="rounded-2xl border border-[#dce8df] bg-[#f4f9f4] p-6"><div className="flex gap-4"><div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl bg-white text-[#397b0a] shadow-sm"><SettingsIcon size={20}/></div><div><h2 className="text-base font-extrabold text-[#082d16]">Administration settings</h2><p className="mt-1 text-sm leading-6 text-[#5f6b63]">Security-sensitive account controls are still enforced by the backend. Browser preferences above are persisted locally for this admin workstation.</p></div></div></section>
 </div></AdminShell>
}
function SettingCard({icon,title,description,on,onChange,locked=false}:{icon:React.ReactNode;title:string;description:string;on?:boolean;onChange?:(v:boolean)=>void;locked?:boolean}){return <section className="rounded-2xl border border-[#e3e8e5] bg-white p-6 shadow-sm"><div className="flex h-11 w-11 items-center justify-center rounded-xl bg-[#edf5ee] text-[#397b0a]">{icon}</div><div className="mt-5"><h2 className="text-base font-extrabold text-[#111827]">{title}</h2><p className="mt-2 max-w-2xl text-sm leading-6 text-[#6b7280]">{description}</p>{locked?<div className="mt-4"><span className="inline-flex max-w-full items-center rounded-full bg-[#f3f5f4] px-3 py-1 text-xs font-bold leading-4 text-[#6b7280]">Backend enforced</span></div>:<div className="mt-4 flex justify-end"><button onClick={()=>onChange?.(!on)} className={`relative h-6 w-11 shrink-0 rounded-full transition ${on?"bg-[#397b0a]":"bg-[#cbd5d0]"}`} aria-label={`Toggle ${title}`}><span className={`absolute top-1 h-4 w-4 rounded-full bg-white transition ${on?"left-6":"left-1"}`}/></button></div>}</div></section>}
