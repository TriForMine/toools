/* (C) Copyright 2009-2013 CNRS (Centre National de la Recherche Scientifique).

Licensed to the CNRS under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The CNRS licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.

*/

/* Contributors:

Luc Hogie (CNRS, I3S laboratory, University of Nice-Sophia Antipolis) 
Aurelien Lancin (Coati research team, Inria)
Christian Glacet (LaBRi, Bordeaux)
David Coudert (Coati research team, Inria)
Fabien Crequis (Coati research team, Inria)
Grégory Morel (Coati research team, Inria)
Issam Tahiri (Coati research team, Inria)
Julien Fighiera (Aoste research team, Inria)
Laurent Viennot (Gang research-team, Inria)
Michel Syska (I3S, Université Cote D'Azur)
Nathann Cohen (LRI, Saclay) 
Julien Deantoin (I3S, Université Cote D'Azur, Saclay) 

*/
 
 package toools.log;

import java.io.PrintStream;

import toools.text.TextUtilities;
import toools.util.Date;

public class TimeStampingPrintStream extends PrintStream
{

	public TimeStampingPrintStream(PrintStream out)
	{
		super(out);
	}

	@Override
	public void println(String x)
	{
		super.println(TextUtilities.prefixEachLineBy(x, Date.now() + "\t"));
	}

	@Override
	public void println(boolean x)
	{
		super.println(Date.now() + "\t" + x);
	}

	@Override
	public void println(char x)
	{
		super.println(Date.now() + "\t" + x);
	}

	@Override
	public void println(int x)
	{
		super.println(Date.now() + "\t" + x);
	}

	@Override
	public void println(long x)
	{
		super.println(Date.now() + "\t" + x);
	}

	@Override
	public void println(float x)
	{
		super.println(Date.now() + "\t" + x);
	}

	@Override
	public void println(char[] x)
	{
		super.println(Date.now() + "\t" + x);
	}

	@Override
	public void println(Object x)
	{
		println(x.toString());
	}

	public static void timeStampStandardIOStreams()
	{
		System.setOut(new TimeStampingPrintStream(System.out));
		System.setErr(new TimeStampingPrintStream(System.err));
	}
}
