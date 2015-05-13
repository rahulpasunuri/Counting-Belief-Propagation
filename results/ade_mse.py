f_name = "tuffy_ade.txt"
f = open(f_name, "r")
lines = f.readlines()
f.close()

res = {}

for l in lines:
	words = l.split()	
	prob = float(words[0])	
	res[words[1].replace("adverse(","").replace(")", "").replace('"',"").lower()]=prob


drugs = [
"drug(ANTIPSYCHOTIC)",
"drug(ANTIBIOTIC)",
"drug(ANTIEPILEPTIC)",
"drug(BENZODIAZEPINE)",
"drug(BISPHOSPHONATE)",
"drug(AMPHOTERICINB)",
"drug(ACEINHIBITOR)",
"drug(TRICYCLICANTIDEPRESSANT)",
"drug(WARFARIN)"
]

effects = [
"effect(BLEEDING)",
"effect(ACUTELIVERFAILURE)",
"effect(HIPFRACTURE)",
"effect(UPPERGIULCER)",
"effect(ACUTEMYOCARDIALINFARCTION)",
"effect(ACUTERENALFAILURE)",
"effect(Aplasticanemia)",
"effect(ANGIOEDEMA)"
]

#missing effects - anemia and hospitalization..

pos = [
"ACEINHIBITORANGIOEDEMA",
"ANTIEPILEPTICAplasticanemia",
"ANTIBIOTICACUTELIVERFAILURE",
"BENZODIAZEPINEHIPFRACTURE",
"WARFARINBLEEDING",
"TRICYCLICANTIDEPRESSANTACUTEMYOCARDIALINFARCTION",
"ANTIPSYCHOTICACUTEMYOCARDIALINFARCTION",
"AMPHOTERICINBACUTERENALFAILURE",
"BISPHOSPHONATEUPPERGIULCER",
]

#preprecessing on above lists..
for i in range(len(drugs)):
	drugs[i] = drugs[i].replace(")", "").replace("drug(","").lower()

for i in range(len(effects)):
	effects[i] = effects[i].replace(")", "").replace("effect(","").lower()


for i in range(len(pos)):
	pos[i]=pos[i].lower()

cross = []

for d in drugs:
	for e in effects:
		cross.append(d+e)

		
for p in pos:
	if p not in cross:
		print "something's wrong"
		print p		
		
for p in pos:
	if p not in res:
		print "something's wrong"
		print p		


mse=0
count=0
for p in cross:
	val=0
	if p in pos:
		val = 1-res[p]		
	else:
		val = res[p]
		count=count+1
	mse = mse + val*val
		
mse = mse/72
print mse
print count
#for k in res:
	#print res[k],"\t",k
	
