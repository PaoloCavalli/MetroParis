package it.polito.tdp.metroparis.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jgrapht.traverse.GraphIterator;

import it.polito.tdp.metroparis.db.MetroDAO;

public class Model {

	
	private Graph<Fermata, DefaultEdge> graph;
	private List <Fermata> fermate;
	private Map <Integer,Fermata> fermateIdMap;
	
	public Model() {
		this.graph = new SimpleDirectedGraph <>(DefaultEdge.class);
		
		MetroDAO dao = new MetroDAO();
		// CREAZIONE DEI VERTICI
		this.fermate= dao.getAllFermate();
		this.fermateIdMap = new HashMap<>();
// con un IdMap ottengo l'oggetto fermata da una chiave di fermata
		for(Fermata f : this.fermate) {
			fermateIdMap.put(f.getIdFermata(), f);
		}
		Graphs.addAllVertices(this.graph, this.fermate);
		
		//System.out.println(this.graph);
		// CREAZIONE DEGLI ARCHI ---> metodo 1 (coppie di vertici)
		/*
		for (Fermata fp : this.fermate) {
			for(Fermata fa : this.fermate) {
				if(dao.fermateConnesse(fp, fa)) {
					this.graph.addEdge(fp, fa);
				}
			}
		}*/
		//CREAZIONE DEGLI ARCHI --> metodo 2 da un vertice, trova tutti i connessi
		/*for (Fermata fp: this.fermate ) {
			List<Fermata> connesse = dao.fermateSuccessive(fp, fermateIdMap);
					
					for(Fermata fa : connesse) {
						this.graph.addEdge(fa, fp);
					}
		}*/
		//METODO 3 ---> chiedo al Db l'elenco di tutti gli archi
		List <CoppiaFermate> coppie = dao.coppieFermate(fermateIdMap);
		for(CoppiaFermate c : coppie) {
			this.graph.addEdge(c.getFp(), c.getFa());
		}
		System.out.format("Grafo caricato con %d vertici %d archi \n", 
				this.graph.vertexSet().size(),
				this.graph.edgeSet().size());
		System.out.println(this.graph);
		
		
		
	}
	/**
	 * Visita l'intero grafo con una strategia BreathFirst
	 * @param source Vertice di partenza
	 * @return l'insieme dei vertici incontrati
	 */
	public List<Fermata>  visitaInAmpiezza(Fermata source) {
		
		
		List<Fermata> visita = new ArrayList<>();
		BreadthFirstIterator<Fermata, DefaultEdge> bfv = new BreadthFirstIterator<>(graph, source);
		while(bfv.hasNext()) {
			visita.add(bfv.next());
		}
		return visita;
	}
	
	public Map<Fermata, Fermata> alberoVisita(Fermata source){
		GraphIterator<Fermata, DefaultEdge> bfv = new BreadthFirstIterator<>(graph, source);
		Map <Fermata, Fermata> albero = new HashMap<>();
		albero.put(source, null);
		
		bfv.addTraversalListener(new TraversalListener<Fermata, DefaultEdge>(){
			@Override
			public void connectedComponentFinished(ConnectedComponentTraversalEvent e) {
			}

			@Override
			public void connectedComponentStarted(ConnectedComponentTraversalEvent e) {
			}

			@Override
			public void edgeTraversed(EdgeTraversalEvent<DefaultEdge> e) {
				//la visita sta considerando un nuovo arco.
				//questo arco ha scoperto un nuovo vertic?
				//se si povenendo da dove?
		     DefaultEdge edge= e.getEdge(); //(a,b) : ho scoperto a partendo da b oppure ho scoperto b da a
		     Fermata a = graph.getEdgeSource(edge);
		     Fermata b = graph.getEdgeTarget(edge);
		    
		     if(albero.containsKey(a) && !albero.containsKey(b)) {
		    	// a è già noto, quindi ho scoperto b provenendo da a
		    	 albero.put(b, a);
		     }else if(albero.containsKey(a) && !albero.containsKey(b)){
		    	 //viceversa
		    	 albero.put(a, b);
		     }
			}

			@Override
			public void vertexTraversed(VertexTraversalEvent<Fermata> e) {
			}

			@Override
			public void vertexFinished(VertexTraversalEvent<Fermata> e) {
			}

		
		});
		while(bfv.hasNext()) {
			bfv.next();
		}
		return albero;
	}
	public List<Fermata>  visitaInProfondita(Fermata source) {
		
		List<Fermata> visita = new ArrayList<>();
		DepthFirstIterator<Fermata, DefaultEdge> dfv = new DepthFirstIterator<>(graph, source);
		while(dfv.hasNext()) {
			visita.add(dfv.next());
		}
		return visita;
	}
	public static void main (String args[]) {
		Model m = new Model();
		List<Fermata> visita1 = m.visitaInAmpiezza(m.fermate.get(0));
		System.out.println(visita1);
		List<Fermata> visita2 = m.visitaInProfondita(m.fermate.get(0));
		System.out.println(visita2);
		
		Map<Fermata,Fermata> albero = m.alberoVisita(m.fermate.get(0));
		for(Fermata f : albero.keySet()) {
			System.out.format("%s --> %s \n",f, albero.get(f));
		}
			
	}
}
