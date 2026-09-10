import fs from 'node:fs';
import path from 'node:path';
import {fileURLToPath} from 'node:url';

// Read-only MCP health check. Does not edit/export/save the active project.
const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const endpoint = 'http://127.0.0.1:3000/bb-mcp';
let session, nextId = 1;
async function call(method, params, notification = false) {
  const request = {jsonrpc: '2.0', method, params};
  if (!notification) request.id = nextId++;
  const response = await fetch(endpoint, {
    method: 'POST', signal: AbortSignal.timeout(10000),
    headers: {'content-type': 'application/json', accept: 'application/json, text/event-stream', ...(session ? {'mcp-session-id': session} : {})},
    body: JSON.stringify(request)
  });
  if (!response.ok) throw new Error(`MCP ${method}: HTTP ${response.status}`);
  session = response.headers.get('mcp-session-id') ?? session;
  if (notification) return;
  const text = await response.text();
  const messages = response.headers.get('content-type')?.includes('text/event-stream')
    ? text.split(/\r?\n/).filter(line => line.startsWith('data:')).map(line => JSON.parse(line.slice(5)))
    : [JSON.parse(text)];
  const message = messages.find(message => message.id === request.id);
  if (!message || message.error) throw new Error(JSON.stringify(message?.error ?? 'No matching MCP response'));
  if (message.result?.isError) throw new Error(JSON.stringify(message.result.content));
  return message.result;
}
try {
  const handshake = await call('initialize', {protocolVersion: '2024-11-05', capabilities: {}, clientInfo: {name: 'time-mod-health-check', version: '0.1.2'}});
  await call('notifications/initialized', {}, true);
  const tools = await call('tools/list', {});
  if (!tools.tools.some(tool => tool.name === 'get_project_info')) throw new Error('Blockbench project API is missing');
  const result = await call('tools/call', {name: 'get_project_info', arguments: {}});
  const project = JSON.parse(result.content.find(block => block.type === 'text').text);
  const report = {generated_at: new Date().toISOString(), endpoint, server: handshake.serverInfo, tool_count: tools.tools.length, ...project};
  const reportFile = path.join(root, 'build/reports/blockbench-mcp.json');
  fs.mkdirSync(path.dirname(reportFile), {recursive: true});
  fs.writeFileSync(reportFile, JSON.stringify(report, null, 2) + '\n');
  console.log(JSON.stringify(report, null, 2));
} catch (error) {
  console.error('Blockbench MCP check failed. Open Blockbench and enable the installed MCP plugin.\n' + error.message);
  process.exitCode = 1;
}
