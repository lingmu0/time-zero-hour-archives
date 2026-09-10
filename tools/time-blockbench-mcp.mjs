import {pathToFileURL} from 'node:url';
import path from 'node:path';
export async function connect(){
  const endpoint='http://127.0.0.1:3000/bb-mcp';let session,id=0;
  async function call(method,params,notification=false){
    const request={jsonrpc:'2.0',method,params,...(notification?{}:{id:++id})};
    const response=await fetch(endpoint,{method:'POST',signal:AbortSignal.timeout(55000),headers:{
      'content-type':'application/json',accept:'application/json, text/event-stream',
      ...(session?{'mcp-session-id':session}:{})},body:JSON.stringify(request)});
    session=response.headers.get('mcp-session-id')??session;
    if(!response.ok)throw Error('MCP HTTP '+response.status);if(notification)return;
    const raw=await response.text(),messages=response.headers.get('content-type')?.includes('text/event-stream')
      ?raw.split(/\r?\n/).filter(v=>v.startsWith('data:')).map(v=>JSON.parse(v.slice(5))):[JSON.parse(raw)];
    const message=messages.find(v=>v.id===request.id);
    if(!message||message.error)throw Error(JSON.stringify(message?.error??'No response'));
    if(message.result?.isError)throw Error(JSON.stringify(message.result.content));
    return message.result;
  }
  await call('initialize',{protocolVersion:'2024-11-05',capabilities:{},clientInfo:{name:'time-cast-animation',version:'0.1.11'}});
  await call('notifications/initialized',{},true);
  return {list:()=>call('tools/list',{}),tool:(name,args={})=>call('tools/call',{name,arguments:args})};
}
if(process.argv[1]&&pathToFileURL(path.resolve(process.argv[1])).href===import.meta.url){
  const mcp=await connect();
  if(process.argv[2]==='list'){
    const result=await mcp.list();
    console.log(JSON.stringify(result.tools.filter(t=>/eval|import|export|file|camera|format/i.test(t.name)),null,2));
  }else console.log(JSON.stringify(await mcp.tool(process.argv[2],JSON.parse(process.argv[3]??'{}'))));
}
